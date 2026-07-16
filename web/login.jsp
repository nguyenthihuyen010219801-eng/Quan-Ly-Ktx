<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<%
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đăng nhập Ký túc xá</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<%= contextPath %>/frontend/styleLogin.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>

<div class="login-page">

    <div class="left-panel">
        <div class="main-logo" aria-hidden="true"></div>

        <div class="features">
            <div>
                <i class="fa-solid fa-shield-halved"></i>
                <h4>AN TOÀN</h4>
                <p>Bảo mật thông tin</p>
            </div>

            <div>
                <i class="fa-solid fa-users"></i>
                <h4>HIỆU QUẢ</h4>
                <p>Quản lý toàn diện</p>
            </div>

            <div>
                <i class="fa-solid fa-chart-line"></i>
                <h4>TIỆN LỢI</h4>
                <p>Thao tác dễ dàng</p>
            </div>
        </div>
    </div>

    <div class="right-panel">

        <form class="login-box" id="loginForm">
            <div class="decor">◆</div>

            <h2>ĐĂNG NHẬP HỆ THỐNG</h2>

            <div class="input-box">
                <i class="fa-solid fa-user"></i>
                <input type="text" id="username" placeholder="Tên đăng nhập" autocomplete="username" required>
            </div>

            <div class="input-box">
                <i class="fa-solid fa-lock"></i>
                <input type="password" id="password" placeholder="Mật khẩu" autocomplete="current-password" required>
                <i class="fa-solid fa-eye-slash eye" onclick="togglePassword()"></i>
            </div>

            <div class="options">
                <label>
                    <input type="checkbox" checked>
                    Ghi nhớ đăng nhập
                </label>

                <a href="#">Quên mật khẩu?</a>
            </div>

            <button type="submit" id="loginSubmit">
                <i class="fa-solid fa-right-to-bracket"></i>
                ĐĂNG NHẬP
            </button>

            <div class="bottom-icon">
                <i class="fa-solid fa-lock"></i>
            </div>

            <p class="footer-text">
                Hệ thống quản lý ký túc xá
            </p>
        </form>

    </div>

</div>

<script>
    const API_URL = "<%= contextPath %>";

function togglePassword() {
    const password = document.getElementById("password");

    if (password.type === "password") {
        password.type = "text";
    } else {
        password.type = "password";
    }
}

let loginSubmitting = false;

function login(event) {
    event?.preventDefault();
    if (loginSubmitting) return;
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();

    if (username === "" || password === "") {
        alert("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu");
        return;
    }

    const submitButton = document.getElementById("loginSubmit");
    loginSubmitting = true;
    submitButton.disabled = true;

    fetch(API_URL + "/api/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            username: username,
            password: password
        })
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            alert("Đăng nhập thành công");
            window.location.href = "<%= contextPath %>/manage.jsp";
        } else {
            alert(data.message);
        }
    })
    .catch(err => {
        console.log(err);
        alert("Không kết nối được server");
    })
    .finally(() => {
        loginSubmitting = false;
        submitButton.disabled = false;
    });
}

document.getElementById("loginForm").addEventListener("submit", login);
</script>

</body>
</html>
