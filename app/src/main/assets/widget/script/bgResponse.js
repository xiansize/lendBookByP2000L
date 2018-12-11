(function($){
	$.fn.bgresponse = function(options){
		var opts = $.extend({
			bgContainer : $(this),
			bgWidth : 0,
			bgHeight : 0,
		},options || {});
		var bgContainer = opts.bgContainer;
		var	bgWidth = opts.bgWidth;
		var	bgHeight = opts.bgHeight;
		var	currentWidth = bgContainer.width();
		var currentHeight = bgContainer.height();

		function change(){
			bgContainer.css({
				height : currentWidth*bgHeight/bgWidth
			});
		}
		change();
		$(window).on("resize",function(e){
			currentWidth = bgContainer.width();
			currentHeight = bgContainer.height();
			change();
		})
	}
})(Zepto)