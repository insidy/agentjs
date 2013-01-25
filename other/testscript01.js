//

var counter = 0;

agent.log('sms:' + agent.sms);

// Teste de sms
agent.sms.on("sms:income", { 'origin' : '5549' }, function(smsInfo) { 
    agent.log("incomig sms from 5549");
    agent.log(smsInfo);
});

agent.sms.on("sms:income", function(smsInfo) { 
    agent.log("incomig sms:" + counter++);
    agent.log(smsInfo.getOriginAddress() + ":" + smsInfo.getMessage());
});

// Teste de leitura de mp3
agent.log('music: ' + agent.music); 
agent.music.playFromUrl('/Music/Chasing The Sun.mp3');

//Teste de rede
agent.log('network: ' + agent.network); 
agent.network.on('wifi:connected', function(data) { 
    agent.log('conectou'); 
});

// Teste de Notificação
var notif = agent.createNotification('teste'); 
notif.content = 'opa!'; 
notif.send(); 

agent.log('Notificacao enviada');

// Teste de lista de apps
var installedApps = agent.apps.getInstalledApps(); 
agent.log(installedApps.length);

/*for(var i = 0; i < installedApps.length; i++){ 
    log(installedApps[i].packageName); 
} */

// Teste de abertura de app
//agent.apps.launchAppByPackage('com.android.contacts');