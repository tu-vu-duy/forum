(function () {
	var ForumUtils = {
			setMaskLayer = function(id) {
				var portlet = gj('div#'+id);
				if (portlet.exists()) {
					var jmaskLayer =  gj('div.KSMaskLayer');
					var jpopupAction = jmaskLayer.find('span.UIKSPopupAction');
					var jpopupWindow = jpopupAction.find('.UIPopupWindow');
					jmaskLayer.css('width', 'auto').css('height', 'auto');
					if (jpopupWindow.exists()) {
						if (jpopupWindow.css('display') == 'block') {
							jmaskLayer.css('width', (portlet.outerWidth()-3)+'px')
												.css('height', (portlet.outerHeight()-3)+'px');
						}
						var closeButton = jpopupAction.find('.CloseButton');
						if (closeButton.exists()) {
							var newDiv = closeButton.find('div.ClosePopup');
							if (!newDiv.exists()) {
								newDiv = aj('<div><span></span></div>');
								newDiv.addClass('ClosePopup');
								closeButton.append(newDiv);
							}
							var w = closeButton.outerWidth();
							var h = closeButton.outerHeight();
							newDiv.css('width', ((w > 0) ? w : 22) + 'px');
							newDiv.css('height', ((h > 0) ? h : 16) + 'px');
							newDiv.on('click', function(event) {
								jmaskLayer.css('width', 'auto').css('height', 'auto');
							});
						}
					}
					masklayer.on('onselectstart', this.returnFalse);
					masklayer.on('ondragstart', this.returnFalse);
					masklayer.unselectable = "no";
				}
			},
			returnFalse = function() {
				return false;
			}
	};
	
	window.eXo = window.eXo || {};
	window.eXo.forum = window.eXo.forum || {};
	window.eXo.forum.ForumUtils = ForumUtils;
})();