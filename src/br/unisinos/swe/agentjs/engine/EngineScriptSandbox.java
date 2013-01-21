package br.unisinos.swe.agentjs.engine;

import java.util.HashSet;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;

public class EngineScriptSandbox extends ContextFactory {

	@SuppressWarnings("deprecation")
	private static class ScriptContext extends Context {
		long startTime;
	}

	public static class SandboxClassShutter implements ClassShutter {

		private static HashSet<String> _allowedScriptComponents = new HashSet<String>();

		public static final <T> void addAllowedScriptableComponent(
				Class<T> paramClass) {
			_allowedScriptComponents.add(paramClass.getName());
		}

		@Override
		public final boolean visibleToScripts(String className) {
			boolean visible = true;
			
			Object[] params = new Object[1];
			params[0] = className;
			EngineContext.log().debug("trying to access component: %s", params);
			
			if (!_allowedScriptComponents.contains(className)) {
				EngineContext.log().error("couldn't access component: %s", params);
				visible = false;
			}
			
			return visible;
		}

	}

	public static class SandboxNativeJavaObject extends NativeJavaObject {

		private static final long serialVersionUID = -3007555998852645770L;

		public SandboxNativeJavaObject(Scriptable paramScriptable,
				Object paramObject, Class<?> paramClass) {
			super(paramScriptable, paramObject, paramClass);
		}

		public final Object get(String paramString, Scriptable paramScriptable) {

			Object[] params = new Object[2];
			params[0] = this.javaObject.getClass().toString();
			params[1] = paramString;

			EngineContext.log().debug("trying to access method: %s.%s", params);

			if (paramString.equals("getClass")) {
				EngineContext.log().error("Tried to access getClass method - denying the request");
				return NOT_FOUND;
			} else {
				return super.get(paramString, paramScriptable);
			}
		}
	}

	public static class SandboxWrapCreator extends WrapFactory {
		public final Scriptable wrapAsJavaObject(Context paramContext,
				Scriptable paramScriptable, Object paramObject,
				Class<?> paramClass) {
			return new SandboxNativeJavaObject(paramScriptable, paramObject,
					paramClass);
		}
	}

	/*
	 * static { // Initialize GlobalFactory with custom factory
	 * ContextFactory.initGlobal(new EngineScriptContextFactory()); }
	 */

	@Override
	protected Context makeContext() {
		ScriptContext ctx = new ScriptContext();
		// Make Rhino runtime to call observeInstructionCount
		// each 10000 bytecode instructions
		ctx.setInstructionObserverThreshold(10000);
		ctx.setOptimizationLevel(-1); // Ensure Android Compatibility

		ctx.setClassShutter(new SandboxClassShutter());
		ctx.setWrapFactory(new SandboxWrapCreator());

		return ctx;
	}

	@Override
	public boolean hasFeature(Context cx, int featureIndex) {
		// Turn on maximum compatibility with MSIE scripts
		switch (featureIndex) {
		case Context.FEATURE_NON_ECMA_GET_YEAR:
			return true;

		case Context.FEATURE_MEMBER_EXPR_AS_FUNCTION_NAME:
			return true;

		case Context.FEATURE_RESERVED_KEYWORD_AS_IDENTIFIER:
			return true;

		case Context.FEATURE_PARENT_PROTO_PROPERTIES:
			return false;
		}
		return super.hasFeature(cx, featureIndex);
	}

	@Override
	protected void observeInstructionCount(Context cx, int instructionCount) {
		ScriptContext mcx = (ScriptContext) cx;
		long currentTime = System.currentTimeMillis();
		if (currentTime - mcx.startTime > 60 * 1000) {
			// More then 60 seconds from Context creation time:
			// it is time to stop the script.
			// Throw Error instance to ensure that script will never
			// get control back through catch or finally.
			EngineContext.log().error("Killing context loop running for more than 60 seconds");
			throw new Error();
		}
	}

	@Override
	protected Object doTopCall(Callable callable, Context cx, Scriptable scope,
			Scriptable thisObj, Object[] args) {
		ScriptContext mcx = (ScriptContext) cx;
		mcx.startTime = System.currentTimeMillis();

		return super.doTopCall(callable, cx, scope, thisObj, args);
	}

}
