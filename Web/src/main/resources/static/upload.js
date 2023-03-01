//获取图片信息
function detect(img_base64) {
    var xhr = new XMLHttpRequest();
    xhr.open('POST', '/ai/detect', false)
    var data = new FormData()
    data.append("img", img_base64)
    xhr.send(data)
    return xhr.response
}
function test(testText) {
    var xhr = new XMLHttpRequest()
    xhr.open('POST', '/ai/test', false)
    var data = new FormData()
    data.append("img", testText)
    xhr.send(data)
    console.log(xhr.response)
}
function selectImage(input){
    var file = input.files[0]
    const image = new Image()
    image.src = URL.createObjectURL(file)
    document.getElementById("img_window").src=image.src
    let tab=document.getElementById("result_table")
    for(i=tab.rows.length;i>=1;i--)
        tab.deleteRow(0)

    tab.insertRow(0).innerHTML="Waiting..."
    image.onload = function (event){
        URL.revokeObjectURL(this.src)
        const canvas = document.createElement("canvas");
        const ctx = canvas.getContext("2d")
        canvas.height = image.height
        canvas.width = image.width
        ctx.drawImage(image, 0, 0)
        const response = detect(canvas.toDataURL("image/jpeg", 1.0));
        console.log(response)
        parseResponse(response)
    }

}
function parseResponse(response){
    var table = document.getElementById("result_table")
    var res = JSON.parse(response)
    table.deleteRow(0)
    best_line = table.insertRow(0)
    best_line.insertCell(0).innerHTML="Best match:"+res.bestMatch
    best_line.insertCell(1).innerHTML="l2 norm:"+res.norm
    var arr = res.candidates
    for(i=0;i<arr.length;i++){
        line = table.insertRow(i+1)
        line.insertCell(0).innerHTML="Candidate result:"+arr[i].character
        line.insertCell(1).innerHTML="l2 norm:"+arr[i].norm
    }
}
function getBase64Image(img) {
    var canvas = document.createElement("canvas");
    canvas.width = img.width;
    canvas.height = img.height;
    var ctx = canvas.getContext("2d");
    ctx.drawImage(img, 0, 0, img.width, img.height);
    return canvas.toDataURL("image/jpeg")
    // return dataURL.replace("data:image/png;base64,", "");
}
function example(id){
    let tab = document.getElementById("example_tab_"+id)
    for(i=tab.rows.length;i>=1;i--)
        tab.deleteRow(0)
    tab.insertRow(0).innerHTML="Waiting..."
    const response = detect(getBase64Image(document.getElementById("img"+id)))


    console.log(tab)
    var res = JSON.parse(response)
    tab.deleteRow(0)
    best_line = tab.insertRow(0)
    best_line.insertCell(0).innerHTML="Best match:"+res.bestMatch
    best_line.insertCell(1).innerHTML="l2 norm:"+res.norm
    var arr = res.candidates
    for(i=0;i<arr.length;i++){
        line = tab.insertRow(i+1)
        line.insertCell(0).innerHTML="candidate result:"+arr[i].character
        line.insertCell(1).innerHTML="l2 norm:"+arr[i].norm
    }
}