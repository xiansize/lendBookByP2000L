var statusBarfixHeight = 0; 

function $(id){

	return document.getElementById(id);
}
//鎵撳紑window
function GotoWin(url, name){
	if(!url){
		return;
	}
	if(!name){
		name = url;
	}
	api.openWin({
		name:name, 
		url:url
	});
}
//window + frame绐楀彛缁撴瀯涓紝鎵撳紑content鍖哄煙鎵�鍦ㄧ殑frame
function openContent(url, fname,frect){

	if(!url){
		return;
	}
	var fn = fname ? fname : 'content_frm';
	var fr = {};//frame鎵�鍦ㄧ殑rect鍖哄煙
	if(frect){
		fr = frect;
	}else{
		//header楂樺害涓篴pi.css鏍峰紡涓０鏄庣殑44px鍔犱笂娌夋蹈寮忔晥鏋滅殑楂樺害
		var headerH = 44 + 25;
		
		fr.marginTop = headerH;
		
	}
    api.openFrame({
        name: fn,
        url: url,
        bounces: true,
        rect: fr,
    });
}
//褰撳墠绯荤粺鏃堕棿鎴筹紝姣
function curtime(){
	
	return new Date().getTime();
}

function fixStatusBar(){
	var el = $('header');
	if(!el){
		return;
	}
    var sysType = api.systemType;
    var ver = api.systemVersion;
    if(sysType == 'ios'){
        var num = parseInt(ver, 10);
        if (num >= 7) {
            el.style.paddingTop = '20px';
            statusBarfixHeight = 20;
        }
    }else if(sysType == 'android'){
        //var num = parseFloat(ver);
        //if(num >= 4.4){
        //    el.style.paddingTop = '25px';
        //    statusBarfixHeight = 25;
        //}
    }
};