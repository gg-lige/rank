<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<script type="text/javascript">
	
	window.onload=function(){
		//1.获取a j节点，并对其添加onclick函数响应
		document.getElementsByTagName("a")[0].onclick=function(){
			
			//3。创建一个XMLHttpRequest对象
			var request=new XMLHttpRequest();
			//4.准备发送请求的的数据：url
			var url= this.href+"?time="+new Date();
			var method="GET";
			//5.调用XMLHttpRequest对象的open方法
			request.open(method,url);
			request.setRequestHeader("contentType","application/x-www-form-urlencoded");
			//6.调用XMLHttpRequest对象的send方法
			request.send(name='lg');
			//7.为XMLHttpRequest对象添加onreadystatechange响应函数
			request.onreadystatechange=function(){
			//8.判断响应是否完成，XMLHttpRequest对象的readyState属性值为4的时候
				if(request.readyState==4){
			//9.再判断响应是否可用，XMLHttpRequest对象的statues属性值为200
					if(request.status ==200||request.status==304){
			//10.打印响应结果：responseText
						alert(request.responseText);}
				}
				
			}
			
			
			
			//2.取消a节点的默认行为
			return false;
		}
		
		
	}

</script>

</head>
<body>

<a href="helloajax.txt">HelloAjax</a>
		

</body>
</html>