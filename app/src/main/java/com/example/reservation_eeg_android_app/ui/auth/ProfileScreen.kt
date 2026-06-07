package com.example.reservation_eeg_android_app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.reservation_eeg_android_app.model.FamilyMember
import com.example.reservation_eeg_android_app.model.UserProfile
import com.example.reservation_eeg_android_app.ui.auth.viewmodel.AuthViewModel
import com.example.reservation_eeg_android_app.ui.theme.ReservationeegandroidappTheme
import com.example.reservation_eeg_android_app.ui.util.AppTextField
import com.example.reservation_eeg_android_app.ui.util.PhoneNumberTransformation
import com.example.reservation_eeg_android_app.ui.util.ResidentIdTransformation
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    onNavigateToFamilyMembers: () -> Unit,
) {
    val sessionStatus by viewModel.sessionStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val familyMembers by viewModel.familyMembers.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()
    
    ProfileContent(
        sessionStatus = sessionStatus,
        isLoading = isLoading,
        error = error,
        userProfile = userProfile,
        familyMembers = familyMembers,
        updateSuccess = updateSuccess,
        onUpdateProfile = { viewModel.updateProfile(it) },
        onSignOut = { viewModel.signOut() },
        onSignIn = { email, password -> viewModel.signIn(email, password) },
        onSignUp = { email, password -> viewModel.signUp(email, password) },
        onSignInWithGoogle = { viewModel.signInWithGoogle() },
        onSignInWithKakao = { viewModel.signInWithKakao() },
        onNavigateToFamilyMembers = onNavigateToFamilyMembers,
        onClearError = viewModel::clearError
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    sessionStatus: SessionStatus,
    isLoading: Boolean,
    error: String?,
    userProfile: UserProfile?,
    familyMembers: List<FamilyMember>,
    updateSuccess: Boolean,
    onUpdateProfile: (UserProfile) -> Unit,
    onSignOut: () -> Unit,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onSignInWithGoogle: () -> Unit,
    onSignInWithKakao: () -> Unit,
    onNavigateToFamilyMembers: () -> Unit,
    onClearError: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var isEditing by remember { mutableStateOf(value = false) }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            snackbarHostState.showSnackbar("프로필 정보가 저장되었습니다.")
            isEditing = false
        }
    }

    LaunchedEffect(error) {
        error?.let {
            val displayError = when {
                it.contains("invalid_credentials", ignoreCase = true) -> "이메일 또는 비밀번호가 올바르지 않습니다."
                it.contains("user_already_exists", ignoreCase = true) -> "이미 가입된 이메일입니다."
                it.contains("network", ignoreCase = true) -> "네트워크 연결을 확인해주세요."
                else -> it.split("\n").firstOrNull() ?: it // Show only first line of long errors
            }
            snackbarHostState.showSnackbar(displayError)
            onClearError()
        }
    }

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.surface
        )
    )

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { 
                    Text(
                        if (isEditing) "정보 수정" else "Profile", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 24.sp
                    ) 
                },
                navigationIcon = {
                    if (isEditing) {
                        IconButton(onClick = { isEditing = false }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            ) 
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBackground)
                .padding(innerPadding)
        ) {
            when (sessionStatus) {
                is SessionStatus.Authenticated -> {
                    val currentProfile = userProfile ?: UserProfile(id = sessionStatus.session.user?.id)
                    if (isEditing) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            ProfileDetailForm(
                                profile = currentProfile,
                                isLoading = isLoading,
                                onUpdate = onUpdateProfile,
                                onCancel = { isEditing = false }
                            )
                        }
                    } else {
                        ProfileOverview(
                            profile = currentProfile,
                            familyMembers = familyMembers,
                            onEditClick = { isEditing = true },
                            onSignOut = onSignOut,
                            onNavigateToFamilyMembers = onNavigateToFamilyMembers
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AuthForm(
                            isLoading = isLoading,
                            onSignIn = onSignIn,
                            onSignUp = onSignUp,
                            onSignInWithGoogle = onSignInWithGoogle,
                            onSignInWithKakao = onSignInWithKakao
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileOverview(
    profile: UserProfile,
    familyMembers: List<FamilyMember>,
    onEditClick: () -> Unit,
    onSignOut: () -> Unit,
    onNavigateToFamilyMembers: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Card with modern look
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar with dynamic initials or icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = profile.name.take(1).uppercase()
                    if (initials.isNotBlank()) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = profile.name.ifBlank { "이름을 설정해주세요" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = profile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (profile.address.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.LocationOn, 
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = profile.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onEditClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Family Section
        SectionHeader(
            title = "가족 추가",
            onAddClick = onNavigateToFamilyMembers
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                if (familyMembers.isEmpty()) {
                    ProfileMenuItem(
                        icon = Icons.Default.People, 
                        label = "가족 구성원을 등록해주세요",
                        onClick = onNavigateToFamilyMembers
                    )
                } else {
                    familyMembers.forEachIndexed { index, member ->
                        ProfileMenuItem(
                            icon = Icons.Default.Person,
                            label = "${member.name} (${member.relationship})",
                            onClick = onNavigateToFamilyMembers
                        )
                        if (index < familyMembers.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        TextButton(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("로그아웃", fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SectionHeader(
    title: String,
    onAddClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        if (onAddClick != null) {
            IconButton(onClick = onAddClick, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Add, 
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector, 
    label: String, 
    onClick: () -> Unit = {},
    iconColor: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    modifier = Modifier.size(20.dp),
                    tint = iconColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label, 
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, 
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ProfileDetailForm(
    profile: UserProfile,
    isLoading: Boolean,
    onUpdate: (UserProfile) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember(profile) { mutableStateOf(profile.name) }
    var sex by remember(profile) { mutableStateOf(profile.sex) }
    var residentId by remember(profile) { mutableStateOf(profile.residentId) }
    var address by remember(profile) { mutableStateOf(profile.address) }
    var phoneNumber by remember(profile) { mutableStateOf(profile.phoneNumber) }
    var email by remember(profile) { mutableStateOf(profile.email) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        AppTextField(
            value = name, 
            onValueChange = { name = it }, 
            label = "이름", 
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = Icons.Default.Person
        )
        
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "성별", 
                style = MaterialTheme.typography.labelLarge, 
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("남성", "여성").forEach { option ->
                    val isSelected = sex == option
                    FilterChip(
                        selected = isSelected,
                        onClick = { sex = option },
                        label = { 
                            Text(
                                option, 
                                modifier = Modifier.fillMaxWidth(), 
                                textAlign = TextAlign.Center
                            ) 
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        AppTextField(
            value = residentId,
            onValueChange = { residentId = it.filter { char -> char.isDigit() }.take(13) },
            label = "주민등록번호",
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = "123456-1234567",
            visualTransformation = ResidentIdTransformation(),
            leadingIcon = Icons.Default.Badge
        )
        
        AppTextField(
            value = address, 
            onValueChange = { address = it }, 
            label = "주소", 
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = Icons.Default.Home
        )
        
        AppTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it.filter { char -> char.isDigit() }.take(11) },
            label = "전화번호",
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            placeholder = "010-0000-0000",
            visualTransformation = PhoneNumberTransformation(),
            leadingIcon = Icons.Default.Phone
        )
        
        AppTextField(
            value = email,
            onValueChange = { email = it },
            label = "이메일",
            modifier = Modifier.fillMaxWidth(),
            enabled = profile.email.isBlank(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            leadingIcon = Icons.Default.Email
        )

        val isFormValid = name.isNotBlank() && phoneNumber.isNotBlank() && residentId.isNotBlank()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                enabled = !isLoading
            ) {
                Text("취소")
            }

            Button(
                onClick = {
                    onUpdate(
                        profile.copy(
                            name = name,
                            sex = sex,
                            residentId = residentId,
                            address = address,
                            phoneNumber = phoneNumber,
                            email = email
                        )
                    )
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading && isFormValid,
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("저장하기", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AuthForm(
    isLoading: Boolean,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onSignInWithGoogle: () -> Unit,
    onSignInWithKakao: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isSignUp by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Even smaller Logo/Icon
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                if (isSignUp) "계정 생성" else "로그인",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            AppTextField(
                value = email,
                onValueChange = { email = it },
                label = "이메일",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                leadingIcon = Icons.Default.Email
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            AppTextField(
                value = password,
                onValueChange = { password = it },
                label = "비밀번호",
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                leadingIcon = Icons.Default.Lock,
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    val description = if (passwordVisible) "Hide password" else "Show password"
                    
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description, modifier = Modifier.size(20.dp))
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (isSignUp) onSignUp(email, password)
                    else onSignIn(email, password)
                },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (isSignUp) "가입하기" else "로그인", fontWeight = FontWeight.Bold)
                }
            }
            
            TextButton(
                onClick = { isSignUp = !isSignUp },
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    if (isSignUp) "이미 계정이 있으신가요? 로그인" else "계정이 없으신가요? 회원가입",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), thickness = 0.5.dp)
                Text(
                    "OR", 
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                HorizontalDivider(modifier = Modifier.weight(1f), thickness = 0.5.dp)
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSignInWithGoogle,
                    modifier = Modifier.weight(1f).height(44.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Google", style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = onSignInWithKakao,
                    modifier = Modifier.weight(1f).height(44.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFEE500),
                        contentColor = Color(0xFF191919)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("카카오", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}


@OptIn(kotlin.time.ExperimentalTime::class)
@Preview(showBackground = true)
@Composable
fun ProfileScreenAuthenticatedPreview() {
    ReservationeegandroidappTheme {
        ProfileContent(
            sessionStatus = SessionStatus.Authenticated(
                session = UserSession(
                    accessToken = "",
                    refreshToken = "",
                    expiresIn = 3600,
                    tokenType = "",
                    user = UserInfo(id = "user123", email = "test@example.com", aud = "", createdAt = null, updatedAt = null)
                )
            ),
            isLoading = false,
            error = null,
            userProfile = UserProfile(
                id = "user123",
                name = "홍길동",
                email = "test@example.com",
                address = "서울특별시 강남구",
                phoneNumber = "010-1234-5678"
            ),
            familyMembers = listOf(
                FamilyMember(id = 1, name = "김영희", relationship = "배우자"),
                FamilyMember(id = 2, name = "홍철수", relationship = "자녀")
            ),
            updateSuccess = false,
            onUpdateProfile = {},
            onSignOut = {},
            onSignIn = { _, _ -> },
            onSignUp = { _, _ -> },
            onSignInWithGoogle = {},
            onSignInWithKakao = {},
            onNavigateToFamilyMembers = {},
            onClearError = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenUnauthenticatedPreview() {
    ReservationeegandroidappTheme {
        ProfileContent(
            sessionStatus = SessionStatus.NotAuthenticated(),
            isLoading = false,
            error = null,
            userProfile = null,
            familyMembers = emptyList(),
            updateSuccess = false,
            onUpdateProfile = {},
            onSignOut = {},
            onSignIn = { _, _ -> },
            onSignUp = { _, _ -> },
            onSignInWithGoogle = {},
            onSignInWithKakao = {},
            onNavigateToFamilyMembers = {},
            onClearError = {}
        )
    }
}
