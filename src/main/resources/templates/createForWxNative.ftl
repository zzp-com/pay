<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>支付</title>
</head>
<body>
<div id="myQrcode"></div>
<div id="orderId" hidden>${orderId}</div>
<div id="returnUrl" hidden>${returnUrl}</div>


<script src="https://cdn.bootcss.com/jquery/1.6.1/jquery.min.js"></script>
<script src="https://cdn.bootcss.com/jquery.qrcode/1.0/jquery.qrcode.min.js"></script>

<script>
    jQuery('#myQrcode').qrcode({
        text:"${codeUrl}"
    });

    $(function () {
        //定時器
        setInterval(function () {
            console.log("开始查询支付状态");
            $.ajax({
                url:"/pay/queryByOrderId",
                data:{
                    'orderId':$('#orderId').text()
                },
                success:function (result) {
                    console.log(result);
                    if(result.platformStatus === 'SUCCESS' && result.platformStatus != null){
                        //跳转
                        location.href = $('#returnUrl').text()
                    }
                },
                error:function (result) {
                    console.log(result);
                    //alert(result);
                }
            })
        })
    })
</script>

</body>
</html>