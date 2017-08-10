<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1,user-scalable=no" />
	<meta http-equiv="Expires" content="0">
	<meta http-equiv="Pragma" content="no-cache">
	<meta http-equiv="Cache-control" content="no-cache">
	<meta http-equiv="Cache" content="no-cache">
	<title>输入取票号</title>
	<link rel="stylesheet" type="text/css" href="css/common.css">
	<script type="text/javascript" src="js/jquery.min.js"></script>
	<script type="text/javascript" src="js/jquery-ui.js"></script>
	<script type="text/javascript">
		jQuery.browser={};(function(){jQuery.browser.msie=false; jQuery.browser.version=0;if(navigator.userAgent.match(/MSIE ([0-9]+)./)){ jQuery.browser.msie=true;jQuery.browser.version=RegExp.$1;}})();
	</script>
	<link href="css/mui.min.css" rel="stylesheet" />
	<script src="js/fpj.js" type="text/javascript"></script>

	<style type="text/css">
		#code {
			width: 60%;
			float: left;
		}
		#cwts {
			color: red;
			margin-left: 23px;
			opacity: 0;
		}
	</style>
</head>
<body id="index" onload="load()">
	<header>
		<img src="images/hkyy.png"/>
	</header>

	<section class="data-tip">
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;感谢您对上海宏康医院的支持，自2017年7月1日起我们将提供"增值税电子普通发票"，电子发票与纸质发票具有同等法律效力，请输入收款收据上提供的发票提取码，及时获得电子发票。
	</section>

	<section>
		<form class="mui-input-group">
			<div class="mui-input-row">
				<input type="text" id="khh" class="mui-input-clear" placeholder="请输入您的取票号">
			</div>
			<div class="mui-input-row">
				<input type="text" id="code" name="code" placeholder="请输入右侧验证码">
				<img src="image.jsp" class="data-yzm" name="randImage" id="randImage" onclick="loadimage()"/>
			</div>
		</form>
	</section>

	<p id="cwts">验证码输入错误</p>

	<button id="Button1" onclick="tiqu()" class="mui-btn mui-btn-primary mui-btn-block">提&nbsp;&nbsp;交</button>
	<input id="gsdm" name="gsdm" type="hidden" />
	<footer>
		<p class="company">上海容津信息技术有限公司</p>
		<p class="help"><a href="bangzhu.html">遇到问题?</a></p>
	</footer>

	<script type="text/javascript">
	  	var sUserAgent = navigator.userAgent.toLowerCase();
	    var bIsIpad = sUserAgent.match(/ipad/i) == "ipad";
	    var bIsIphoneOs = sUserAgent.match(/iphone os/i) == "iphone os";
	    var bIsMidp = sUserAgent.match(/midp/i) == "midp";
	    var bIsUc7 = sUserAgent.match(/rv:1.2.3.4/i) == "rv:1.2.3.4";
	    var bIsUc = sUserAgent.match(/ucweb/i) == "ucweb";
	    var bIsAndroid = sUserAgent.match(/android/i) == "android";
	    var bIsCE = sUserAgent.match(/windows ce/i) == "windows ce";
	    var bIsWM = sUserAgent.match(/windows mobile/i) == "windows mobile"
        var gsdm = location.href.split('?')[1].split('&')[0].split('=')[1];

		function loadimage() {
			document.getElementById("randImage").src = "image.jsp?" + Math.random();
		}
		function load() {
			document.getElementById("randImage").src = "image.jsp?" + Math.random();
			$('#code').val('');
            $('#gsdm').val(gsdm);
		}
		function tiqu() {
			var khh = $('#khh').val();
			var code = $('#code').val();
            var gsdm =   $('#gsdm').val();
			$.post(
				"tqyz",
				{
                    "tqm" : khh,
                    "code" : code,
                    "gsdm":  gsdm
				},
				function(data) {
					if (data.num == 2) {
                        if (!(bIsIpad || bIsIphoneOs || bIsMidp || bIsUc7 || bIsUc || bIsAndroid || bIsCE || bIsWM) ){
                            window.open("mbxfp.html?gsdm="+data.gsdm+"&&tqm="+khh+"&&time=" + new Date());
                        }else{
                            window.location.href ="mbxfp.html?gsdm="+data.gsdm+"&&tqm="+khh+"&&time=" + new Date();
                        }
					} else if (data.num == 6) {
                        btnArray = [ '确定' ];
                        firm = "您提取的申请已提交,我们正在处理,请稍等!";
                        title = "提示";
                        jAlert(firm, title);
					}  else if (data.num == 4) {
                        $('#cwts').html("您输入的验证码不正确");
                        loadimage();
					}
				}
			);
		}
		function tiaozhuan() {
			window.location.href = "bangzhu.html?time=" + new Date();
		}
	</script>
</body>
</html>