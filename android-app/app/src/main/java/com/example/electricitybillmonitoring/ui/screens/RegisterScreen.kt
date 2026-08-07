package com.example.electricitybillmonitoring.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.electricitybillmonitoring.data.model.User
import com.example.electricitybillmonitoring.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreeToTerms by remember { mutableStateOf(false) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    var localError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Account", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D47A1))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Graphic lightbulb illustration
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bulb circle design
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFE3F2FD), RoundedCornerShape(40.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(45.dp)) {
                        // Draw lightbulb outline
                        val bulbPath = Path().apply {
                            moveTo(size.width * 0.5f, 0f)
                            cubicTo(
                                size.width * 0.2f, 0f,
                                size.width * 0.2f, size.height * 0.6f,
                                size.width * 0.35f, size.height * 0.75f
                            )
                            lineTo(size.width * 0.35f, size.height * 0.9f)
                            lineTo(size.width * 0.65f, size.height * 0.9f)
                            lineTo(size.width * 0.65f, size.height * 0.75f)
                            cubicTo(
                                size.width * 0.8f, size.height * 0.6f,
                                size.width * 0.8f, 0f,
                                size.width * 0.5f, 0f
                            )
                            close()
                        }
                        drawPath(path = bulbPath, color = Color(0xFFFFD54F))

                        // Draw lightning inside
                        val boltPath = Path().apply {
                            moveTo(size.width * 0.55f, size.height * 0.2f)
                            lineTo(size.width * 0.35f, size.height * 0.5f)
                            lineTo(size.width * 0.52f, size.height * 0.5f)
                            lineTo(size.width * 0.45f, size.height * 0.8f)
                            lineTo(size.width * 0.65f, size.height * 0.45f)
                            lineTo(size.width * 0.48f, size.height * 0.45f)
                            close()
                        }
                        drawPath(path = boltPath, color = Color(0xFF0D47A1))
                    }
                }

                Column {
                    Text(
                        text = "Create Your Account",
                        color = Color(0xFF121D3A),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Join us to manage your electricity bill and track your usage easily.",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Inputs Form
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Full Name
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Full Name", color = Color(0xFF121D3A), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Enter your full name") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Mobile Number
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Mobile Number", color = Color(0xFF121D3A), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .height(56.dp)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Phone, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                Text("+91", color = Color(0xFF121D3A), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }

                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = it },
                            placeholder = { Text("Enter your mobile number") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Email Address
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Email Address", color = Color(0xFF121D3A), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Enter your email address") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Password
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Password", color = Color(0xFF121D3A), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Create a password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color.Gray) },
                        trailingIcon = {
                            Text(
                                text = if (passwordVisible) "Hide" else "Show",
                                color = Color(0xFF0D47A1),
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clickable { passwordVisible = !passwordVisible },
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF0D47A1),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Use 8+ characters with letters, numbers & symbols",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }

                // Confirm Password
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Confirm Password", color = Color(0xFF121D3A), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = { Text("Confirm your password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color.Gray) },
                        trailingIcon = {
                            Text(
                                text = if (confirmPasswordVisible) "Hide" else "Show",
                                color = Color(0xFF0D47A1),
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clickable { confirmPasswordVisible = !confirmPasswordVisible },
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Checkbox Agree
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = agreeToTerms,
                        onCheckedChange = { agreeToTerms = it }
                    )
                    Text(
                        text = "I agree to the Terms & Conditions and Privacy Policy",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val displayError = localError ?: error
                if (displayError != null) {
                    Text(
                        displayError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        localError = null
                        if (name.isBlank() || mobile.isBlank() || email.isBlank() || password.isBlank()) {
                            localError = "Please fill all fields"
                        } else if (!agreeToTerms) {
                            localError = "Please agree to the Terms & Conditions"
                        } else if (password != confirmPassword) {
                            localError = "Passwords do not match"
                        } else {
                            val newUser = User(
                                fullName = name,
                                email = email,
                                mobileNumber = mobile,
                                password = password,
                                role = "consumer"
                            )
                            viewModel.register(newUser, onRegisterSuccess)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
                ) {
                    if (loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color.LightGray)
                    )
                    Text(
                        text = " OR ",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color.LightGray)
                    )
                }

                // Google Sign Up Button
                OutlinedButton(
                    onClick = { /* Handle Google register */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "G",
                            color = Color(0xFFDB4437),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Sign up with Google",
                            color = Color(0xFF121D3A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                // Footer Link
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Already have an account? ",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Login",
                        color = Color(0xFF0D47A1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }
            }
        }
    }
}
