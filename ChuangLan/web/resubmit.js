var id = ingage.getSelectedIds('detail');
ingage.openLoading();
ingage.conn.remoteCall({
    url: '/data/v1/objects/customize/update',
    type: 'post',
    contentType: 'application/json;charset=utf-8',
    data: JSON.stringify({
        id: id,
        'customItem75__c': '1'
    }),
    success: function (a) {
        console.log(a);
        if (a != undefined && a.status != undefined && a.status == 0) {
            alert("更新成功");
            ingage.reload();
        } else {
            alert("更新失败");
        }
        ingage.closeLoading();
    },
    error: function (a) {
        alert("更新失败");
        ingage.closeLoading();
    }
});