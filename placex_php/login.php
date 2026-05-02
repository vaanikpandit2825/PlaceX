<?php
require 'db.php';

$email = trim($_POST['email'] ?? '');
$pass  = trim($_POST['password'] ?? '');
$role  = trim($_POST['role'] ?? 'student'); // student | admin | recruiter

if (!$email || !$pass) {
    echo "Missing fields";
    exit;
}

if ($role === 'admin') {

    $stmt = $conn->prepare("SELECT admin_id, name FROM admin WHERE email=? AND password=?");
    $stmt->bind_param("ss", $email, $pass);
    $stmt->execute();
    $res = $stmt->get_result();

    if ($row = $res->fetch_assoc()) {
        echo json_encode([
            'status'   => 'Success',
            'role'     => 'admin',
            'admin_id' => $row['admin_id'],
            'name'     => $row['name']
        ]);
    } else {
        echo "Invalid admin credentials";
    }

} elseif ($role === 'recruiter') {

    $stmt = $conn->prepare("
        SELECT R.recruiter_id, R.name, R.company_id, C.company_name, C.is_approved
        FROM Recruiter R
        INNER JOIN Company C ON R.company_id = C.company_id
        WHERE R.email=? AND R.password=?
    ");
    $stmt->bind_param("ss", $email, $pass);
    $stmt->execute();
    $res = $stmt->get_result();

    if ($row = $res->fetch_assoc()) {
        echo json_encode([
            'status'       => 'Success',
            'role'         => 'recruiter',
            'recruiter_id' => $row['recruiter_id'],
            'name'         => $row['name'],
            'company_id'   => $row['company_id'],
            'company_name' => $row['company_name'],
            'is_approved'  => (int)$row['is_approved']
        ]);
    } else {
        echo "Invalid recruiter credentials";
    }

} else {

    $stmt = $conn->prepare("SELECT student_id, name, is_placed FROM Student WHERE email=? AND password=?");
    $stmt->bind_param("ss", $email, $pass);
    $stmt->execute();
    $res = $stmt->get_result();

    if ($row = $res->fetch_assoc()) {
        echo json_encode([
            'status'     => 'Success',
            'role'       => 'student',
            'student_id' => $row['student_id'],
            'name'       => $row['name'],
            'is_placed'  => (int)$row['is_placed']
        ]);
    } else {
        echo "Invalid email or password";
    }
}

$conn->close();
?>