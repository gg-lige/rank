<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>

<script type="text/javascript" src="jquery-1.7.2.js"></script>
<script type="text/javascript">
	
	$(function(){
		$("a").click(function(){
			//使用get方法处理ajax
			var url = this.href;
			var args= {"time":new Date()};
			
			//url: post或get均可
			//args:JSON格式
			//function:回调函数：当响应结束时，回调函数被触发。响应结果在data中。
			$.post(url,args,function(data){
				var name =$(data).find("name").text();
				var email =$(data).find("email").text();
				var website =$(data).find("website").text();
				
				$("#content").empty().append("<h2><a href='mailto:"+email+"'>"+name+"</a></h2>")
									 .append("<a href='"+website+"'>"+website+"</a>");
				
			});
			
			return false;
		});
	})



</script>

</head>
<body>
	
	<a href="data.xml">lg</a>

	
	<div id="content"></div>
</body>
</html>