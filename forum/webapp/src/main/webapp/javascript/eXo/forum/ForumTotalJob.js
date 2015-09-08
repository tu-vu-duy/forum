/**
 * This module use for auto send pedding jobs of Adminstrators/moderators - waiting for validate.
 * Use cCometD 3 to send messages information.
 */
(function(cCometD, $, window, document) {
  var ForumTotalJob = {
    currentUser : '',
    // Register cCometD
    init : function(eXoUser, eXoToken, contextName) {
      if (String(eXoToken)) {
        var me = ForumTotalJob;
        if(!me.Cometd) me.Cometd = cCometD;
        var loc = window.location;
        me.Cometd.configure({
            url: loc.protocol + '//' + loc.hostname + (loc.port ? ':' + loc.port : '')  + '/' + contextName + '/cometd',
            'exoId': eXoUser, 'exoToken': eXoToken
        });
        if (me.currentUser !== eXoUser || me.currentUser === '') {
          me.currentUser = eXoUser;
          me.Cometd.subscribe('/eXo/Application/Forum/messages', null, function(eventObj) {
            me.alarm(eventObj);
          });
        }//end user
      }
    },
    // Parse result and update new information on UIForumActionBar
    alarm : function(eventObj) {
      var a = JSON.parse(eventObj.data);
      var pr = $('#PendingJob');
      if (pr.exists()) {
        var span = pr.find('span');
        span.html(a.categoryName);
        if(parseInt(a.categoryName) > 0) {
          span.css('text-decoration', 'blink');
        } else {
          span.css('text-decoration', 'none');
        }
      }
      return;
    }
  };

  return ForumTotalJob;
})(cCometd, gj, window, document);
