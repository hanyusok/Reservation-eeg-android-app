package com.example.reservation_eeg_android_app.ui.community

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.reservation_eeg_android_app.data.supabaseClient
import com.example.reservation_eeg_android_app.model.CommunityPost
import com.example.reservation_eeg_android_app.model.communityCategories
import com.example.reservation_eeg_android_app.ui.community.viewmodel.CommunityViewModel
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    viewModel: CommunityViewModel = viewModel(),
    isAuthenticated: Boolean,
    onNavigateToLogin: () -> Unit
) {
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isPostSuccess by viewModel.isPostSuccess.collectAsState()

    CommunityContent(
        posts = posts,
        isLoading = isLoading,
        error = error,
        isPostSuccess = isPostSuccess,
        isAuthenticated = isAuthenticated,
        onFetchPosts = { viewModel.fetchPosts() },
        onCreatePost = { cat, title, content, image -> viewModel.createPost(cat, title, content, image) },
        onUpdatePost = { id, cat, title, content, image -> viewModel.updatePost(id, cat, title, content, image) },
        onLikePost = { viewModel.likePost(it) },
        onDeletePost = { viewModel.deletePost(it) },
        onResetSuccess = { viewModel.resetSuccess() },
        onNavigateToLogin = onNavigateToLogin
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityContent(
    posts: List<CommunityPost>,
    isLoading: Boolean,
    error: String?,
    isPostSuccess: Boolean,
    isAuthenticated: Boolean,
    onFetchPosts: () -> Unit,
    onCreatePost: (String, String, String, ByteArray?) -> Unit,
    onUpdatePost: (Int, String, String, String, ByteArray?) -> Unit,
    onLikePost: (Int) -> Unit,
    onDeletePost: (Int) -> Unit,
    onResetSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var selectedCategory by remember { mutableStateOf("전체") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingPost by remember { mutableStateOf<CommunityPost?>(null) }

    LaunchedEffect(isPostSuccess) {
        if (isPostSuccess) {
            showCreateDialog = false
            editingPost = null
            onResetSuccess()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("EEG 케어 서클", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onFetchPosts) {
                        Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                    }
                }
            }

            // Categories
            CategoryRow(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            if (isLoading && posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val filteredPosts = if (selectedCategory == "전체") {
                    posts
                } else {
                    posts.filter { it.category == selectedCategory }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredPosts) { post ->
                        PostCard(
                            post = post,
                            onLike = { post.id?.let { onLikePost(it) } },
                            onDelete = { post.id?.let { onDeletePost(it) } },
                            onEdit = { editingPost = post }
                        )
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { 
                if (isAuthenticated) {
                    showCreateDialog = true 
                } else {
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "로그인이 필요한 서비스입니다.",
                            actionLabel = "로그인"
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            onNavigateToLogin()
                        }
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "글쓰기")
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (showCreateDialog) {
            CreatePostDialog(
                onDismiss = { showCreateDialog = false },
                onPost = { cat, title, content, img -> onCreatePost(cat, title, content, img) }
            )
        }

        if (editingPost != null) {
            CreatePostDialog(
                postToEdit = editingPost,
                onDismiss = { editingPost = null },
                onPost = { cat, title, content, img -> 
                    editingPost?.id?.let { onUpdatePost(it, cat, title, content, img) }
                }
            )
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryRow(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val allCategories = listOf("전체") + communityCategories
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        allCategories.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category) }
            )
        }
    }
}

@Composable
fun PostCard(
    post: CommunityPost,
    onLike: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val currentUserId = remember { supabaseClient.auth.currentUserOrNull()?.id }
    val isOwner = post.userId == currentUserId
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(post.userName.take(1), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.userName, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        post.createdAt?.let { dateStr ->
                            Text(" • ", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            val formattedDate = try {
                                val zdt = ZonedDateTime.parse(dateStr)
                                zdt.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                            } catch (e: Exception) {
                                dateStr.take(10)
                            }
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                if (isOwner) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "옵션")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("수정") },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("삭제", color = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                            )
                        }
                    }
                }
            }

            // Image
            if (post.imageUrl != null) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Content
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.clickable { onLike() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (post.likesCount > 0) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (post.likesCount > 0) Color.Red else LocalContentColor.current
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${post.likesCount}", style = MaterialTheme.typography.labelMedium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Comment, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("0", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostDialog(
    postToEdit: CommunityPost? = null,
    onDismiss: () -> Unit,
    onPost: (String, String, String, ByteArray?) -> Unit
) {
    var selectedCat by remember { mutableStateOf(postToEdit?.category ?: communityCategories[0]) }
    var title by remember { mutableStateOf(postToEdit?.title ?: "") }
    var content by remember { mutableStateOf(postToEdit?.content ?: "") }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (postToEdit == null) "정보 공유하기" else "글 수정하기") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("카테고리 선택", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(communityCategories) { cat ->
                        FilterChip(
                            selected = selectedCat == cat,
                            onClick = { selectedCat = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("제목") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("내용") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                // Image Selection UI
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    if (selectedImageUri != null || postToEdit?.imageUrl != null) {
                        Box(modifier = Modifier.height(120.dp).fillMaxWidth()) {
                            AsyncImage(
                                model = selectedImageUri ?: postToEdit?.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { 
                                    selectedImageUri = null
                                    // Note: In a real app, you might want to track if the existing image was removed
                                },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "이미지 제거", tint = Color.White)
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { launcher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("사진 추가")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val imageBytes = selectedImageUri?.let { uri ->
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    }
                    onPost(selectedCat, title, content, imageBytes)
                },
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text(if (postToEdit == null) "게시하기" else "수정하기")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CommunityScreenPreview() {
    val samplePosts = listOf(
        CommunityPost(
            id = 1,
            userName = "홍길동",
            category = "질환정보",
            title = "EEG 검사 전 주의사항",
            content = "EEG 검사를 받기 전에는 머리를 깨끗이 감고 오시는 것이 좋습니다. 젤이 잘 붙어야 정확한 측정이 가능하거든요.",
            likesCount = 5,
            createdAt = "2023-10-01T10:00:00Z"
        ),
        CommunityPost(
            id = 2,
            userName = "김철수",
            category = "케어팁",
            title = "가정에서 하는 케어 방법",
            content = "가정에서도 충분한 휴식과 규칙적인 생활 습관을 유지하는 것이 중요합니다.",
            likesCount = 12,
            createdAt = "2023-10-02T11:30:00Z"
        )
    )
    
    com.example.reservation_eeg_android_app.ui.theme.ReservationeegandroidappTheme {
        CommunityContent(
            posts = samplePosts,
            isLoading = false,
            error = null,
            isPostSuccess = false,
            isAuthenticated = true,
            onFetchPosts = {},
            onCreatePost = { _, _, _, _ -> },
            onUpdatePost = { _, _, _, _, _ -> },
            onLikePost = {},
            onDeletePost = {},
            onResetSuccess = {},
            onNavigateToLogin = {}
        )
    }
}

