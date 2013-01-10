(function() {
   function createNamespace(namespace) {
      parts = namespace.split('.');
      var base = window;
      
      for(var i = 0; i < namespace.length; i++) {
         base[parts[i]] = base[parts[i]] || {};
         base = base[parts[i]];
      }
      
      return base; //return resulting namespace object
   }
   
   var namespace = createNamespace("org.openqa.selenium");
   
   if(namespace.pendingCalls)
      return;
   
   namespace.pendingCalls = 0;
   
   XMLHttpRequest.prototype.oldSend = XMLHttpRequest.prototype.send;
   
   var newSend = function(data) {
       var xhr = this;
       
       namespace.pendingCalls++;
       
       var callFinished = function() {
          namespace.pendingCalls--;
       };
       
       xhr.addEventListener("abort", callFinished, false);
       xhr.addEventListener("load", callFinished, false);
       xhr.addEventListener("error", callFinished, false);
       xhr.addEventListener("timeout", callFinished, false);
       
       xhr.oldSend(data);
   }
   
   XMLHttpRequest.prototype.send = newSend;
}());