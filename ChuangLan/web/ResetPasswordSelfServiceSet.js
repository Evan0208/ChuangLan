var id = ingage.getSelectedIds('detail');
ingage.openLoading();
ingage.conn.remoteCall({
    url: '/script-api/customopenapi/password-reset',
    method: 'POST',
    data: {id: id},
    success: function (data) {
        if (data.status == 0) {
            ingage.confirm( data.result);
            ingage.closeLoading();
            ingage.reload();
        }
        else {
            alert("重置密码错误");
            ingage.closeLoading();
        }
    },
    error: function (a) {
        alert("重置密码错误。");
        ingage.closeLoading();
    }
});