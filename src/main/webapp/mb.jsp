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
	<title>输入提取码</title>
	<link rel="stylesheet" type="text/css" href="css/common.css">
	<link href="css/mui.min.css" rel="stylesheet" />
	<link href="css/jquery.alerts.css" rel="stylesheet" />
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
		.mui-input-group .mui-input-row {
		    height: 45px;
		}
		#us {
			text-decoration:none;
			color:#000000;
		}

	</style>
</head>
<body id="index" onload="load()">
<header>
	<div id="img">
		<img id="logo" src="images/rjxx.jpg"/>
	</div>
</header>
	
	<section class="data-tip">
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;感谢您对<a id="us">我们</a>
		的支持,自2017年8月1日起我们将提供"增值税电子普通发票",电子发票与纸质发票具有同等法律效力,请输入收款收据上提供的发票提取码,及时获得电子发票。
	</section>

	<section>
		<form class="mui-input-group">
			<div class="mui-input-row">
				<input type="text" id="tqm" class="mui-input-clear" placeholder="点击此输入发票提取码">
			</div>
			<input id="gsdm" name="gsdm" type="hidden" />
			<div class="mui-input-row">
				<input type="text" id="code" name="code" placeholder="请输入右侧验证码">
				<img src="image.jsp" name="randImage" class="data-yzm" id="randImage" onclick="loadimage()"/>
			</div>
		</form>
		<p id="cwts">验证码输入错误</p>

		<button id="Button1" onclick="tiqu()" style="padding: 8px 0;" class="mui-btn mui-btn-primary mui-btn-block">提&nbsp;&nbsp;交</button>

	</section>

	<footer>
		<p class="company">上海容津信息技术有限公司</p>
		<p class="help"><a href="bangzhu.html">遇到问题?</a></p>
	</footer>
	

	<script type="text/javascript" src="js/jquery.min.js"></script>
	<script type="text/javascript" src="js/jquery-ui.js"></script>
	<script type="text/javascript">
		jQuery.browser={};(function(){jQuery.browser.msie=false; jQuery.browser.version=0;if(navigator.userAgent.match(/MSIE ([0-9]+)./)){ jQuery.browser.msie=true;jQuery.browser.version=RegExp.$1;}})();
	</script>
	<script type="text/javascript" src="js/jquery.alerts.js"></script>
	<script src="js/fpj.js" type="text/javascript"></script>

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
		};
		function load() {
			document.getElementById("randImage").src = "image.jsp?" + Math.random();
			$('#code').val('');

		}
        $(function () {
            var gsdm = location.href.split('?')[1].split('&')[0].split('=')[1];
            $('#gsdm').val(gsdm);
            var gsdm= $('#gsdm').val();
			if(gsdm!=null && gsdm!=""){
                $("#logo").attr("src", "images/" + gsdm + ".png");
			}
            $.post(
                "common/getGsxx",
                {
                    "gsdm":gsdm
				},
                function (data) {
					if(data!=null && data!=""){
					$('#us').text(data);
					}
                }
            )

        })
		function tiqu() {
			var tqm = $('#tqm').val();
			var num = /^(([1-9][0-9]*)|(([0]\.\d{1,2}|[1-9][0-9]*\.\d{1,2})))$/;		
			var code = $('#code').val();
            var gsdm =   $('#gsdm').val();

			if(tqm==null||tqm==""){
                firm = "请输入发票提取码！";
                title = "提示";
                jAlert(firm, title);
                return;
			}
			if(code==null||code==""){
                firm = "请输入右侧验证码！";
                title = "提示";
                jAlert(firm, title);
                return;
			}
			if(gsdm==null){
                firm = "请重新访问此页面！";
                title = "提示";
                jAlert(firm, title);
                return;
			}else if(gsdm=="ldyx") {
                if (tqm.length != 19) {
                    firm = "输入发票提取码不符合规定！";
                    title = "提示";
                    jAlert(firm, title);
                    return;
                }
            }
			$.post(
				"tqyz",
				{
					"tqm" : tqm,
					"code" : code,
					"gsdm":  gsdm
				}, 
				function(data) {
					if (data.num == 1) {
						firm = "您提取的发票尚未开具,请检查提取码是否正确,或稍后再试!";
						title = "提示";
                        jAlert(firm,title);
                        $('#cwts').css('opacity','0');
                        loadimage();
					}else if (data.num == 2) {
                        if (!(bIsIpad || bIsIphoneOs || bIsMidp || bIsUc7 || bIsUc || bIsAndroid || bIsCE || bIsWM) ){
                                window.location.href= "mbxfp.html?gsdm="+data.gsdm+"&&tqm="+tqm+"&&time=" + new Date();
                        }else{
                                window.location.href = "mbxfp.html?gsdm="+data.gsdm+"&&tqm="+tqm+"&&time=" + new Date();
						}
					}else if (data.num == 4) {
                        loadimage();
                        $('#cwts').css('opacity','1');
					}else if (data.num == 5) {
                        window.location.href = "mbfptt.html?gsdm="+data.gsdm+"&&tqm="+data.tqm+"&&time=" + new Date();
					}else if (data.num == 6) {
                        firm = "您提取的申请已提交,我们正在处理,请稍等!";
                        title = "提示";
                        jAlert(firm, title);
                        loadimage();
                        $('#cwts').css('opacity','0');
                    }else if (data.num == 11) {
                        loadimage();
                        $('#cwts').html("输入的提取码不正确");
                    }else  if(data.num ==12){
                        firm = data.msg;
                        title = "提示";
                        jAlert(firm, title);
                        loadimage();
                        $('#cwts').css('opacity','0');
					}
				}
			);
		}
	</script>
</body>
</html>