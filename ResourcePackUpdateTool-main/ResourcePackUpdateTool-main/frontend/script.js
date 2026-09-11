        const API_BASE = 'http://localhost:21856/api/v1';
        let selectedFile = null;
        
        const fileInput = document.getElementById('file-input');
        const fileName = document.getElementById('file-name');
        const tokenInput = document.getElementById('token-input');
        const uploadBtn = document.getElementById('upload-btn');
        const downloadBtn = document.getElementById('download-btn');
        const refreshStatusBtn = document.getElementById('refresh-status-btn');
        const deleteBtn = document.getElementById('delete-btn');
        const uploadProgress = document.getElementById('upload-progress');
        const alertContainer = document.getElementById('alert-container');
        const fileSize = document.getElementById('file-size');
        const fileSizeFormatted = document.getElementById('file-size-formatted');
        const lastUpdated = document.getElementById('last-updated');
        const lastUpdatedFormatted = document.getElementById('last-updated-formatted');
        const deleteConfirmation = document.getElementById('delete-confirmation');
        const cancelDeleteBtn = document.getElementById('cancel-delete-btn');
        const confirmDeleteBtn = document.getElementById('confirm-delete-btn');

        document.addEventListener('DOMContentLoaded', () => {
            getFileStatus();
            fileInput.addEventListener('change', handleFileSelect);
            uploadBtn.addEventListener('click', handleUpload);
            refreshStatusBtn.addEventListener('click', getFileStatus);
            deleteBtn.addEventListener('click', showDeleteConfirmation);
            cancelDeleteBtn.addEventListener('click', hideDeleteConfirmation);
            confirmDeleteBtn.addEventListener('click', handleDelete);
        });

        function handleFileSelect(event) {
            selectedFile = event.target.files[0];
            if (selectedFile) {
                if (!selectedFile.name.endsWith('.zip')) {
                    showAlert('请选择ZIP格式的文件', 'error');
                    fileInput.value = '';
                    selectedFile = null;
                    fileName.textContent = '选择ZIP文件';
                    return;
                }
                
                fileName.textContent = selectedFile.name;
                showAlert(`已选择文件: ${selectedFile.name}`, 'info');
            } else {
                fileName.textContent = '选择ZIP文件';
            }
        }

        async function handleUpload() {
            if (!selectedFile) {
                showAlert('请先选择要上传的文件', 'error');
                return;
            }
            
            const token = tokenInput.value.trim();
            if (!token) {
                showAlert('请输入认证令牌', 'error');
                return;
            }
            
            const hashedToken = await sha256(token);
            const formData = new FormData();
            formData.append('file', selectedFile);
            uploadProgress.style.width = '0%';
            
            try {
                const response = await fetch(`${API_BASE}/upload`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${hashedToken}`
                    },
                    body: formData
                });
                
                const result = await response.json();
                
                if (result.success) {
                    showAlert('文件上传成功', 'success');
                    uploadProgress.style.width = '100%';
                    getFileStatus();
                } else {
                    showAlert(`上传失败: ${result.message}`, 'error');
                }
            } catch (error) {
                console.error('上传错误:', error);
                showAlert('上传过程中发生错误', 'error');
            }
        }

        async function getFileStatus() {
            try {
                const response = await fetch(`${API_BASE}/status`);
                const result = await response.json();
                
                if (result.success) {
                    if (result.data) {
                        fileSize.textContent = `${result.data.size} 字节`;
                        fileSizeFormatted.textContent = result.data.sizeFormatted || '--';

                        const rawDate = result.data.updatedAt;
                        lastUpdated.textContent = rawDate || '--';

                        if (rawDate && rawDate !== '--') {
                            const formattedDate = formatDateTime(rawDate);
                            lastUpdatedFormatted.textContent = formattedDate;
                        } else {
                            lastUpdatedFormatted.textContent = '--';
                        }
                    } else {
                        fileSize.textContent = '--';
                        fileSizeFormatted.textContent = '--';
                        lastUpdated.textContent = '--';
                        lastUpdatedFormatted.textContent = '--';
                    }
                } else {
                    showAlert(`获取状态失败: ${result.message}`, 'error');
                }
            } catch (error) {
                console.error('获取状态错误:', error);
                showAlert('获取文件状态时发生错误', 'error');
            }
        }

        function showDeleteConfirmation() {
            deleteConfirmation.style.display = 'flex';
        }

        function hideDeleteConfirmation() {
            deleteConfirmation.style.display = 'none';
        }

        async function handleDelete() {
            hideDeleteConfirmation();
            
            const token = tokenInput.value.trim();
            if (!token) {
                showAlert('请输入认证令牌', 'error');
                return;
            }
            
            const hashedToken = await sha256(token);
            
            try {
                const response = await fetch(`${API_BASE}/rmfile`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${hashedToken}`
                    }
                });
                
                const result = await response.json();
                
                if (result.success) {
                    if (result.message === 'no need') {
                        showAlert('服务器上不存在资源文件，无需删除', 'info');
                    } else {
                        showAlert('文件删除成功', 'success');
                        getFileStatus();
                    }
                } else {
                    let errorMessage = '删除失败';
                    

                    switch (result.message) {
                        case 'EBUSY':
                            errorMessage = '文件正被使用，无法删除';
                            break;
                        case 'EPERM':
                            errorMessage = '权限不足，无法删除文件';
                            break;
                        case 'ENOENT':
                            errorMessage = '文件不存在，无法删除';
                            break;
                        case 'EACCES':
                            errorMessage = '访问被拒绝，无法删除文件';
                            break;
                        default:
                            errorMessage = `删除失败: ${result.message}`;
                    }
                    
                    showAlert(errorMessage, 'error');
                }
            } catch (error) {
                console.error('删除错误:', error);
                showAlert('删除过程中发生错误', 'error');
            }
        }


        function formatDateTime(isoString) {
            const date = new Date(isoString);
            

            const year = date.getFullYear();
            const month = date.getMonth() + 1; 
            const day = date.getDate();
            const hours = date.getHours();
            const minutes = date.getMinutes();
            const seconds = date.getSeconds();
            

            return `${year}/${month}/${day} ${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        }


        async function sha256(message) {
            const msgBuffer = new TextEncoder().encode(message);
            const hashBuffer = await crypto.subtle.digest('SHA-256', msgBuffer);
            const hashArray = Array.from(new Uint8Array(hashBuffer));
            return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
        }


        function showAlert(message, type) {
            const alert = document.createElement('div');
            alert.className = `alert ${type}`;
            alert.textContent = message;
            
            alertContainer.appendChild(alert);
            alert.style.display = 'block';
            

            setTimeout(() => {
                alert.remove();
            }, 3000);
        }