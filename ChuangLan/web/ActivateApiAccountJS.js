var id = ingage.getSelectedIds('detail');
ingage.openLoading();
ingage.conn.remoteCall({
    url: '/script-api/customopenapi/apiAccount-activate',
    method: 'POST',
    data: {id: id},
    success: function (data) {
        if (data.status == 0) {
            ingage.confirm("接口账号：" + data.result);
            ingage.closeLoading();
            ingage.reload();
        } else {
            alert("激活API主账号错误");
            ingage.closeLoading();
        }
    },
    error: function (a) {
        alert("激活API主账号报错。");
        ingage.closeLoading();
    }
});