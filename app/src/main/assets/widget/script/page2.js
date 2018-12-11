var result;
$(function(){
	 /*遮罩层*/
   	$(".mask").css("height",$(document).height());
      
	hintShow();  /*显示 提交成功 弹出框*/
//	hintShow(".subfail"); /*显示 提交失败 弹出框*/
 	function hintShow(){
  		$(".confirm").tap(function(){
  			var text = document.getElementById('text').value;
  			api.sendEvent({
            	name: 'sendEmail',
            	extra: text
           	});
  		})
  	}
 	hintHide(); /*隐藏 提交成功 弹出框*/
//	hintHide(".subfail");/*隐藏 提交失败 弹出框*/
 	function hintHide(){

 		$(".secconfirm").tap(function(){
 			$(".mask").hide();
 			if(result){
          		$(".subsuc").hide();
          		sendH5EventToNative();
         	}
         	else{
         		$(".subfail").hide();
         	}

 		})
 	}
})

function showDialog(ret){
	result =ret;
	if(result){
		$(".mask").show();
    	$(".subsuc").show();
    }
    else{
    	$(".mask").show();
    	$(".subfail").show();
   	}
}

function sendH5EventToNative(){

	var ext ="";
	api.sendEvent({
	    name: 'success',
	    extra: ext
	});
}
	
    