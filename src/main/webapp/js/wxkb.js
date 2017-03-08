var access_token// 两小时刷新一次,每天限制2000次
var expires_in;
// 第一步获取access_token
function access_token() {
	$.ajax({
		async : false,
		url : 'https://api.weixin.qq.com/cgi-bin/token',
		data : {
			"grant_type" : 'client_credential',
			"appid" : 'wx9abc729e2b4637ee',
			"secret" : '6415ee7a53601b6a0e8b4ac194b382eb'// secret 值
		},
		method : 'get',
		success : function(data) {
			access_token = data.access_token;
			expires_in = data.expires_in;
			alert(data.access_token);
		},
		error : function(data) {
			alert(data.errcode + data.errmsg);
		}
	});
}


// 第三步 选取卡卷背景颜色
// 第四步 创建卡卷
function cjkj() {
	$
			.ajax({
				async : false,
				url : 'https://api.weixin.qq.com/card/create?access_token='
						+ access_token,
				data : {
					"card" : {
						"card_type" : "GIFT",
						"groupon" : {
							"base_info" : {
								"logo_url" : "http://mmbiz.qpic.cn/mmbiz_png/SGSA3AKjmxdJREpvW9kv6ERMHyUmT4NbWbEHK5p3naVquws5mrL04Bq7zibtTL8d9iccKcZdOf1OYPibKzDib2rDsw/0",
								"brand_name" : "中原地产",
								"code_type" : "CODE_TYPE_TEXT",
								"title" : "电子发票提取",
								"sub_title" : "ddh",
								"color" : "Color010",
								"notice" : "请点击预览,下载",
								"service_phone" : "020-88888888",
								"description" : "请尽快提取\n请尽快提取\n请尽快提取",
								"date_info" : {// 使用日期
									"type" : "DATE_TYPE_FIX_TIME_RANGE",
									"begin_timestamp" : 1397577600,
									"end_timestamp" : 1422724261
								},
								"sku" : {
									"quantity" : 100000000
								},
								"get_limit" : 3,
								"use_custom_code" : true,
								"bind_openid" : false,
								"can_share" : true,
								"can_give_friend" : true,
								"location_id_list" : [ 123, 12321, 345345 ],
								"center_title" : "顶部居中按钮",
								"center_sub_title" : "按钮下方的wording",
								"center_url" : "www.qq.com",
								"custom_url_name" : "查看发票",
								"custom_url" : "http://www.qq.com",
								"custom_url_sub_title" : "6个汉字tips",
								"promotion_url_name" : "更多优惠",
								"promotion_url" : "http://www.qq.com",
								"source" : "大众点评"
							},
							"deal_detail" : "以下锅底2选1（有菌王锅、麻辣锅、大骨锅、番茄锅、清补 凉锅、酸菜鱼锅可选）：\n大锅1份 12元\n小锅2份 16元 "
						}
					}
				},
				method : 'post',
				success : function(data) {
					// alert(data.errcode,data.errmsg,data.card_id);
				},
				error : function(data) {
					alert(data.errcode + data.errmsg);
				}
			});
}