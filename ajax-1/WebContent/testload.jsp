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
			//使用load方法处理ajax
			var url = this.href +" h2";
		//	var url = this.href;
			var args= {"time":new Date()};
			//任何一个html节点都可以使用load方法来加载ajax,结果将直接插入html中。
			$("#content").load(url,args);
			
			return false;
		});
	})



</script>

</head>
<body>

	<a href="helloajax.txt">HelloAjax</a>
	<a href="hellolg.txt">Hello lg</a>
	
	<div id="content"></div>

</body>
</html>