function download(fileId) {
    fetch(`file-service/download/${fileId}`)
        .then(response => {
            if (response.status == 204) {
                setTimeout(() => download(fileId), 1000);
            } else if (response.status == 200) {

                let disposition = response.headers.get('Content-Disposition');
                let fileName = disposition.replace(/attachment; filename\*=UTF-8''/g, '');

                response.blob().then(blob => {
                    let objectURL = window.URL.createObjectURL(blob);
                    let a = document.createElement('a');
                    a.href = objectURL;
                    a.download = fileName;
                    document.body.appendChild(a);
                    a.click();
                    a.remove();
                });

            }
        });
}

function main() {
    $('button').click(function(e) {
        let requestBody = {
            "fileName": "my_file.txt",
            "content": ["Hello World!", "This is my content."]
        };

        fetch('/file-service/generate-file', {
                method: 'POST',
                body: JSON.stringify(requestBody),
                headers: { 'Content-Type': 'application/json' }
            })
            .then(response => response.text())
            .then(fileId => {
                download(fileId);
            })
            // download("data:text/html,HelloWorld!", "helloWorld.txt")
    });
}

main();