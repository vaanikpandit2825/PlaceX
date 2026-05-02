<?php
// register.php  — handles both Student and Recruiter registration
require 'db.php';

$role = trim($_POST['role'] ?? 'student');

if ($role === 'recruiter') {
    // ── RECRUITER REGISTRATION ──────────────────────────────
    $name         = trim($_POST['name']         ?? '');
    $email        = trim($_POST['email']        ?? '');
    $pass         = trim($_POST['password']     ?? '');
    $company_name = trim($_POST['company_name'] ?? '');
    $industry     = trim($_POST['industry']     ?? '');
    $location     = trim($_POST['location']     ?? '');
    $website      = trim($_POST['website']      ?? '');
    $description  = trim($_POST['description']  ?? '');

    if (!$name || !$email || !$pass || !$company_name) {
        echo "Missing required fields"; exit;
    }

    // Check duplicate email
    $chk = $conn->prepare("SELECT 1 FROM Recruiter WHERE email=?");
    $chk->bind_param("s", $email);
    $chk->execute();
    if ($chk->get_result()->num_rows > 0) { echo "Email already registered"; exit; }

    $conn->begin_transaction();
    try {
        // Insert Company (is_approved=0 → pending admin approval)
        $s1 = $conn->prepare("INSERT INTO Company(company_name,industry,location,website,description,is_approved) VALUES(?,?,?,?,?,0)");
        $s1->bind_param("sssss", $company_name, $industry, $location, $website, $description);
        $s1->execute();
        $company_id = $conn->insert_id;

        // Insert Recruiter
        $s2 = $conn->prepare("INSERT INTO Recruiter(name,email,password,company_id) VALUES(?,?,?,?)");
        $s2->bind_param("sssi", $name, $email, $pass, $company_id);
        $s2->execute();

        $conn->commit();
        echo "Success"; // Recruiter registered, pending company approval
    } catch (Exception $e) {
        $conn->rollback();
        echo "Registration failed: " . $e->getMessage();
    }

} else {
    // ── STUDENT REGISTRATION ────────────────────────────────
    $name   = trim($_POST['name']   ?? '');
    $email  = trim($_POST['email']  ?? '');
    $branch = trim($_POST['branch'] ?? '');
    $cgpa   = floatval($_POST['cgpa'] ?? 0);
    $pass   = trim($_POST['password'] ?? '');

    if (!$name || !$email || !$branch || !$pass) {
        echo "Missing required fields"; exit;
    }

    $chk = $conn->prepare("SELECT 1 FROM Student WHERE email=?");
    $chk->bind_param("s", $email);
    $chk->execute();
    if ($chk->get_result()->num_rows > 0) { echo "Email already registered"; exit; }

    $stmt = $conn->prepare("INSERT INTO Student(name,email,branch,cgpa,password) VALUES(?,?,?,?,?)");
    $stmt->bind_param("sssds", $name, $email, $branch, $cgpa, $pass);
    if ($stmt->execute()) {
        echo "Success";
    } else {
        echo "Registration failed: " . $conn->error;
    }
}
$conn->close();
?>
