var log = Packages.br.unisinos.swe.agentjs.engine.EngineLogger.i;

var counter = 0;

log('sms:' + agent.sms);

// Teste de sms
agent.sms.on("sms:income", { 'origin' : '5549' }, function(smsInfo) { 
    log("incomig sms from 5549");
    log(smsInfo);
});

agent.sms.on("sms:income", function(smsInfo) { 
    log("incomig sms:" + counter++);
    log(smsInfo);
});

// Teste de leitura de mp3
log('music: ' + agent.music); 
agent.music.playFromUrl('/Music/Chasing The Sun.mp3');

//Teste de rede
log('network: ' + agent.network); 
agent.network.on('wifi:connected', function(data) { 
    log('conectou'); 
});

// Teste de Notificação
var notif = agent.createNotification('teste'); 
notif.content = 'opa!'; 
notif.send(); 

log('Notificacao enviada');

// Teste de lista de apps
//var installedApps = agent.apps.getInstalledApps(); 
//log(installedApps[i].length);

/*for(var i = 0; i < installedApps.length; i++){ 
    log(installedApps[i].packageName); 
} */

// Teste de abertura de app
//agent.apps.launchAppByPackage('com.android.contacts');