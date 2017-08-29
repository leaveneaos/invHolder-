(function(window){var svgSprite="<svg>"+""+'<symbol id="icon-weixin" viewBox="0 0 1024 1024">'+""+'<path d="M274.55 420.483c-46.994 0-86.57-39.575-86.57-86.57s39.576-86.57 86.57-86.57 86.57 39.575 86.57 86.57-37.1 86.57-86.57 86.57z m0-123.671c-19.787 0-37.1 17.314-37.1 37.101s17.313 37.101 37.1 37.101 37.102-17.314 37.102-37.101-14.84-37.101-37.101-37.101z m321.547 123.671c-46.996 0-86.57-39.575-86.57-86.57s39.574-86.57 86.57-86.57 86.57 39.575 86.57 86.57-39.575 86.57-86.57 86.57z m0-123.671c-19.788 0-37.102 17.314-37.102 37.101s17.314 37.101 37.102 37.101 37.101-17.314 37.101-37.101-17.314-37.101-37.101-37.101z m2.473 304.231c-27.208 0-49.469 22.261-49.469 49.47s22.261 49.468 49.469 49.468 49.469-22.261 49.469-49.469-22.261-49.469-49.469-49.469z m185.507 0c-27.207 0-49.468 22.261-49.468 49.47s22.26 49.468 49.468 49.468 49.469-22.261 49.469-49.469-22.261-49.469-49.469-49.469z" fill="" ></path>'+""+'<path d="M435.324 66.783c210.241 0 316.599 153.352 348.753 242.396 12.367 37.101 12.367 54.415 14.84 86.57v19.787l2.474 27.208 24.735 12.367C910.222 502.106 959.69 573.836 959.69 648.04c0 64.309-32.155 126.145-91.517 168.193l-29.681 22.26 12.367 34.629 12.367 37.101-76.676-39.575-14.84-7.42-17.315 2.473c-19.787 4.947-39.575 4.947-59.362 4.947-116.251 0-217.662-61.835-252.29-155.826l-12.367-32.154h-37.102c-14.84 0-19.787 0-39.575-2.474l-14.84-2.473-14.84 7.42-136.04 69.256 27.208-86.57 12.368-34.628-29.682-22.26C116.251 551.574 69.256 465.004 69.256 375.96 66.783 205.295 232.502 66.783 435.324 66.783m0-49.469c-230.03 0-418.01 160.773-418.01 361.12 0 111.305 56.889 207.769 148.406 274.552L98.937 850.86l244.87-121.198c22.26 2.473 29.68 2.473 49.468 4.947 42.049 108.83 160.773 187.98 299.285 187.98 24.735 0 46.996-2.473 69.256-7.42l183.034 91.517-49.468-150.88c69.256-49.468 111.304-123.67 111.304-207.767 0-101.411-66.783-185.508-158.3-234.976-2.473-44.522-2.473-69.256-17.314-118.725-39.574-116.251-165.72-277.024-395.748-277.024z" fill="" ></path>'+""+'<path d="M395.749 744.502c-17.314-34.628-17.314-79.15-17.314-98.937v-4.947c4.947-148.405 140.985-262.183 311.652-262.183 14.84 0 32.155 0 49.469 2.473 29.68 4.947 74.202 12.367 106.357 32.155l-24.734 42.048c-27.208-17.314-71.73-22.26-86.57-24.734-14.84-2.474-29.681-2.474-42.049-2.474-143.459 0-257.236 93.99-262.183 212.715v4.947c0 17.314 0 54.416 12.367 79.15l-46.995 19.787z" fill="" ></path>'+""+"</symbol>"+""+"</svg>";var script=function(){var scripts=document.getElementsByTagName("script");return scripts[scripts.length-1]}();var shouldInjectCss=script.getAttribute("data-injectcss");var ready=function(fn){if(document.addEventListener){if(~["complete","loaded","interactive"].indexOf(document.readyState)){setTimeout(fn,0)}else{var loadFn=function(){document.removeEventListener("DOMContentLoaded",loadFn,false);fn()};document.addEventListener("DOMContentLoaded",loadFn,false)}}else if(document.attachEvent){IEContentLoaded(window,fn)}function IEContentLoaded(w,fn){var d=w.document,done=false,init=function(){if(!done){done=true;fn()}};var polling=function(){try{d.documentElement.doScroll("left")}catch(e){setTimeout(polling,50);return}init()};polling();d.onreadystatechange=function(){if(d.readyState=="complete"){d.onreadystatechange=null;init()}}}};var before=function(el,target){target.parentNode.insertBefore(el,target)};var prepend=function(el,target){if(target.firstChild){before(el,target.firstChild)}else{target.appendChild(el)}};function appendSvg(){var div,svg;div=document.createElement("div");div.innerHTML=svgSprite;svgSprite=null;svg=div.getElementsByTagName("svg")[0];if(svg){svg.setAttribute("aria-hidden","true");svg.style.position="absolute";svg.style.width=0;svg.style.height=0;svg.style.overflow="hidden";prepend(svg,document.body)}}if(shouldInjectCss&&!window.__iconfont__svg__cssinject__){window.__iconfont__svg__cssinject__=true;try{document.write("<style>.svgfont {display: inline-block;width: 1em;height: 1em;fill: currentColor;vertical-align: -0.1em;font-size:16px;}</style>")}catch(e){console&&console.log(e)}}ready(appendSvg)})(window)