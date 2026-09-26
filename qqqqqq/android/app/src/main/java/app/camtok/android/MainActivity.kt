package app.camtok.android

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

private val Paper = Color(0xFFF5F5EF)
private val Ink = Color(0xFF1C1E18)
private val Muted = Color(0xFF71756B)
private val Lime = Color(0xFFD9F36A)
private val Coral = Color(0xFFFF6757)
private val Line = Color(0xFFE3E4DB)

private enum class CamPage { HOME, EXPLORE, INBOX, PROFILE, LIVE, WALLET, CREATOR, SETTINGS }

private data class VideoPost(
    val id: String,
    val handle: String,
    val displayName: String,
    val avatar: String,
    val image: String,
    val caption: String,
    val tags: List<String>,
    val sound: String,
    val likes: Int,
    val comments: Int,
    val videoUri: Uri? = null,
)

private data class Creator(val name: String, val handle: String, val avatar: String)

private val creators = listOf(
    Creator("Noah Ellis", "noahmakesmusic", "photo-1506794778202-cad84cf45f1d"),
    Creator("Kay Williams", "cookinwithkay", "photo-1544005313-94ddf0286df2"),
    Creator("Juniper Park", "juniper.jpeg", "photo-1531123897727-8f129e1688ce"),
    Creator("Alex Kim", "alexmakes", "photo-1500648767791-00dcc994a43e"),
)

private fun seedPosts() = listOf(
    VideoPost("mila", "milaoutside", "Mila Reyes", "photo-1534528741775-53994a69daeb", "photo-1470252649378-9c29740c9fa8", "up before the world. definitely worth it", listOf("morninglight", "outsideclub"), "original sound - mila outside", 24800, 312),
    VideoPost("juniper", "juniper.jpeg", "Juniper Park", "photo-1531123897727-8f129e1688ce", "photo-1470770841072-f978cf4d019e", "a very serious scientific study of cloud shapes", listOf("cloudwatching", "slowdays"), "Bloom - The Paper Kites", 18300, 208),
    VideoPost("benny", "bennysunday", "Ben Carter", "photo-1500648767791-00dcc994a43e", "photo-1473448912268-2022ce9509d8", "took the long way home again. always take the long way.", listOf("trailfinds", "weekend"), "The Woods - Hollow Coves", 42100, 734),
    VideoPost("kay", "cookinwithkay", "Kay Williams", "photo-1544005313-94ddf0286df2", "photo-1490645935967-10de6ba17061", "crispy edges are not optional. here's the 10-minute version", listOf("easyfood", "homecooking"), "just the kitchen sounds", 97600, 2100),
    VideoPost("alex", "alexmakes", "Alex Kim", "photo-1506794778202-cad84cf45f1d", "photo-1498050108023-c5249f4df085", "my setup tour but i finally cleaned my desk first", listOf("desksetup", "smallspaces"), "Night Shift - Lucy Dacus", 63500, 482),
    VideoPost("priya", "sunnysideup", "Priya Singh", "photo-1524504388940-b1c1722653e1", "photo-1519681393784-d120267933ba", "if you can't see the stars, let this be your sign", listOf("nightwalk", "starrysky"), "Space Song - Beach House", 37200, 598),
)

private fun photoUrl(id: String, size: Int = 900) =
    "https://images.unsplash.com/$id?auto=format&fit=crop&w=$size&q=85"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CamTokApp() }
    }
}

@Composable
private fun CamTokApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val posts = remember { mutableStateListOf<VideoPost>().apply { addAll(seedPosts()) } }
    val following = remember { mutableStateListOf("juniper.jpeg", "cookinwithkay") }
    val liked = remember { mutableStateListOf<String>() }
    val saved = remember { mutableStateListOf<String>() }
    var page by remember { mutableStateOf(CamPage.HOME) }
    var followingFeed by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var uploadOpen by remember { mutableStateOf(false) }
    var pickedVideo by remember { mutableStateOf<Uri?>(null) }
    var commentsFor by remember { mutableStateOf<VideoPost?>(null) }
    var commentText by remember { mutableStateOf("") }
    var infoTitle by remember { mutableStateOf<String?>(null) }
    var infoBody by remember { mutableStateOf("") }
    val userComments = remember { mutableStateListOf<String>() }
    var coinBalance by remember { mutableStateOf(2450) }

    fun notify(message: String) { scope.launch { snackbar.showSnackbar(message) } }
    fun showInfo(title: String, body: String) { infoTitle = title; infoBody = body }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
                // Some document providers return temporary access only.
            }
            pickedVideo = uri
        }
    }
    val cameraCapture = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data
        if (result.resultCode == Activity.RESULT_OK && uri != null) {
            pickedVideo = uri
            notify("Camera video ready to add to your local feed")
        } else if (result.resultCode == Activity.RESULT_OK) {
            notify("Camera did not return a video file")
        }
    }

    MaterialTheme(colorScheme = lightColorScheme(primary = Ink, onPrimary = Color.White, secondary = Lime, background = Paper, surface = Color.White, onSurface = Ink)) {
        Scaffold(
            containerColor = Paper,
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                    val items = listOf(
                        Triple(CamPage.HOME, "Home", Icons.Rounded.Home),
                        Triple(CamPage.EXPLORE, "Explore", Icons.Rounded.Explore),
                        Triple(CamPage.HOME, "Create", Icons.Rounded.Add),
                        Triple(CamPage.INBOX, "Inbox", Icons.Rounded.MailOutline),
                        Triple(CamPage.PROFILE, "Profile", Icons.Rounded.PersonOutline),
                    )
                    items.forEachIndexed { index, item ->
                        val isCreate = index == 2
                        NavigationBarItem(
                            selected = !isCreate && page == item.first,
                            onClick = { if (isCreate) uploadOpen = true else page = item.first },
                            icon = {
                                Box(
                                    modifier = if (isCreate) Modifier.size(35.dp).clip(RoundedCornerShape(10.dp)).background(Lime) else Modifier,
                                    contentAlignment = Alignment.Center,
                                ) { Icon(item.third, contentDescription = item.second, modifier = Modifier.size(22.dp)) }
                            },
                            label = { Text(item.second, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                            alwaysShowLabel = true,
                        )
                    }
                }
            },
        ) { insets ->
            Column(Modifier.fillMaxSize().padding(insets)) {
                CamTopBar(onWallet = { page = CamPage.WALLET }, onNotifications = { notify("You're all caught up") })
                when (page) {
                    CamPage.HOME -> HomeScreen(
                        posts = posts.toList(), following = following.toSet(), liked = liked.toSet(), saved = saved.toSet(),
                        followingFeed = followingFeed, query = searchText,
                        onQuery = { searchText = it }, onFollowingFeed = { followingFeed = it },
                        onLike = { id -> if (!liked.remove(id)) liked.add(id) },
                        onSave = { id -> if (!saved.remove(id)) saved.add(id) },
                        onFollow = { handle -> if (!following.remove(handle)) following.add(handle) },
                        onComments = { commentsFor = it }, onShare = { notify("Share link copied when connected to a server") },
                        onLive = { page = CamPage.LIVE }, onTag = { searchText = it },
                    )
                    CamPage.EXPLORE -> ExploreScreen(
                        search = searchText, onSearch = { searchText = it; page = CamPage.HOME },
                        onTag = { searchText = it; page = CamPage.HOME }, onLive = { page = CamPage.LIVE },
                    )
                    CamPage.INBOX -> InboxScreen(onMessage = { notify("Messaging will be available when your account is connected") })
                    CamPage.PROFILE -> ProfileScreen(onOpen = { page = it }, coinBalance = coinBalance)
                    CamPage.LIVE -> LiveScreen(onBack = { page = CamPage.HOME }, onStart = { showInfo("Go LIVE", "Real broadcasts and PK matches need a live video service, creator eligibility, moderation, and real-time chat on CamTok's secure server. This preview does not start a broadcast.") })
                    CamPage.WALLET -> WalletScreen(coinBalance = coinBalance, onBack = { page = CamPage.PROFILE }, onPurchase = { pack -> showInfo("CamTok Coins", "$pack coins can be sold only after secure Google Play Billing and a verified account backend are connected. No charge was made.") }, onPayout = { showInfo("Creator payouts", "Payouts need server-side identity and age verification, tax checks, fraud review, and a connected payout provider. No payment account is connected.") })
                    CamPage.CREATOR -> CreatorScreen(onBack = { page = CamPage.PROFILE }, onPayout = { page = CamPage.WALLET })
                    CamPage.SETTINGS -> SettingsScreen(onBack = { page = CamPage.PROFILE }, onSafety = { showInfo("Account safety", "Blocking, reporting, age-aware defaults, and moderation queues must be enforced on the CamTok service before launch. Local controls cannot protect a shared account.") })
                }
            }
        }
    }

    if (uploadOpen) {
        UploadDialog(
            selectedVideo = pickedVideo,
            onChooseVideo = { videoPicker.launch(arrayOf("video/mp4", "video/webm", "video/quicktime", "video/*")) },
            onRecordVideo = {
                val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
                    putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60)
                }
                if (intent.resolveActivity(context.packageManager) != null) cameraCapture.launch(intent)
                else notify("No camera app is available on this device")
            },
            onDismiss = { uploadOpen = false },
            onPublish = { caption, topic ->
                val uri = pickedVideo
                if (uri == null) notify("Choose a video from your device first")
                else {
                    posts.add(0, VideoPost("your-${System.currentTimeMillis()}", "you", "Your profile", "photo-1534528741775-53994a69daeb", "photo-1519608487953-e999c86e7455", caption.ifBlank { "a little something from today" }, listOf(topic.removePrefix("#").ifBlank { "yourday" }), "original sound - you", 0, 0, uri))
                    following.add("you")
                    pickedVideo = null
                    followingFeed = false
                    searchText = ""
                    page = CamPage.HOME
                    uploadOpen = false
                    notify("Your video is ready on this device")
                }
            },
        )
    }

    commentsFor?.let { post ->
        CommentsDialog(
            post = post,
            comments = userComments,
            value = commentText,
            onValueChange = { commentText = it },
            onDismiss = { commentsFor = null },
            onSend = {
                if (commentText.isNotBlank()) {
                    userComments.add(0, commentText.trim())
                    commentText = ""
                    notify("Comment added on this device")
                }
            },
        )
    }

    if (infoTitle != null) {
        AlertDialog(
            onDismissRequest = { infoTitle = null },
            title = { Text(infoTitle.orEmpty(), fontWeight = FontWeight.Bold) },
            text = { Text(infoBody) },
            confirmButton = { TextButton(onClick = { infoTitle = null }) { Text("Got it") } },
            containerColor = Color.White,
        )
    }
}

@Composable
private fun CamTopBar(onWallet: () -> Unit, onNotifications: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(58.dp).background(Paper).padding(horizontal = 17.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(29.dp).clip(RoundedCornerShape(10.dp, 10.dp, 10.dp, 3.dp)).background(Lime), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
            }
            Text("CamTok", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Ink, letterSpacing = (-0.8).sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TextButtonWithIcon(label = "2.4K", onClick = onWallet)
            IconButton(onClick = onNotifications) { Icon(Icons.Rounded.NotificationsNone, contentDescription = "Notifications", tint = Ink) }
        }
    }
}

@Composable
private fun TextButtonWithIcon(label: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(20.dp), color = Color.White, border = BorderStroke(1.dp, Line)) {
        Row(Modifier.padding(horizontal = 11.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("C", color = Coral, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            Text(label, color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeScreen(
    posts: List<VideoPost>, following: Set<String>, liked: Set<String>, saved: Set<String>,
    followingFeed: Boolean, query: String, onQuery: (String) -> Unit,
    onFollowingFeed: (Boolean) -> Unit, onLike: (String) -> Unit, onSave: (String) -> Unit,
    onFollow: (String) -> Unit, onComments: (VideoPost) -> Unit, onShare: () -> Unit,
    onLive: () -> Unit, onTag: (String) -> Unit,
) {
    val matching = posts.filter { post ->
        (!followingFeed || post.handle in following || post.handle == "you") &&
            (query.isBlank() || listOf(post.handle, post.displayName, post.caption, post.tags.joinToString(" ")).any { it.contains(query.removePrefix("#"), ignoreCase = true) })
    }
    val rankedPosts = if (!followingFeed && query.isBlank()) {
        matching.sortedByDescending { post -> (if (post.handle in following) 1.0 else 0.0) + post.likes.toDouble() / 100_000.0 }
    } else matching
    val pagerState = rememberPagerState(pageCount = { rankedPosts.size.coerceAtLeast(1) })
    LaunchedEffect(followingFeed, query, rankedPosts.size) { pagerState.scrollToPage(0) }

    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQuery,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            singleLine = true,
            placeholder = { Text("Search videos, creators, hashtags", fontSize = 13.sp, color = Muted) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(20.dp), tint = Muted) },
            shape = RoundedCornerShape(12.dp),
        )
        Row(Modifier.fillMaxWidth().padding(top = 7.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            FeedTab("Following", active = followingFeed, onClick = { onFollowingFeed(true) })
            Spacer(Modifier.width(8.dp))
            FeedTab("For you", active = !followingFeed, onClick = { onFollowingFeed(false) })
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onLive, colors = ButtonDefaults.textButtonColors(contentColor = Coral)) {
                Text("LIVE  ·  PK", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)) {
            item { StoryBubble("Your story", "photo-1534528741775-53994a69daeb", isOwn = true) }
            items(creators) { creator -> StoryBubble("@${creator.handle}", creator.avatar) }
        }
        if (rankedPosts.isEmpty()) {
            EmptyMessage(if (followingFeed) "Your Following feed is quiet" else "No videos found", "Try another search or discover a few new creators.")
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 5.dp, bottom = 8.dp),
                key = { index -> rankedPosts[index].id },
                beyondViewportPageCount = 1,
            ) { index ->
                val post = rankedPosts[index]
                VideoCard(
                    post = post,
                    isLiked = post.id in liked,
                    isSaved = post.id in saved,
                    isFollowing = post.handle in following,
                    onLike = { onLike(post.id) },
                    onSave = { onSave(post.id) },
                    onFollow = { onFollow(post.handle) },
                    onComments = { onComments(post) },
                    onShare = onShare,
                    onTag = onTag,
                )
            }
        }
    }
}

@Composable
private fun FeedTab(label: String, active: Boolean, onClick: () -> Unit) {
    Column(Modifier.clickable(onClick = onClick).padding(horizontal = 5.dp, vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = if (active) Ink else Muted, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(Modifier.height(3.dp))
        Box(Modifier.width(if (active) 22.dp else 0.dp).height(3.dp).clip(CircleShape).background(if (active) Coral else Color.Transparent))
    }
}

@Composable
private fun StoryBubble(label: String, avatar: String, isOwn: Boolean = false) {
    Column(Modifier.width(57.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(if (isOwn) Lime else Coral).padding(2.dp).clip(CircleShape).background(Paper).padding(2.dp)) {
            AsyncImage(photoUrl(avatar, 100), contentDescription = label, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
            if (isOwn) Box(Modifier.size(17.dp).align(Alignment.BottomEnd).clip(CircleShape).background(Lime), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Add, contentDescription = "Add story", tint = Ink, modifier = Modifier.size(14.dp))
            }
        }
        Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Muted)
    }
}

@Composable
private fun VideoCard(
    post: VideoPost, isLiked: Boolean, isSaved: Boolean, isFollowing: Boolean,
    onLike: () -> Unit, onSave: () -> Unit, onFollow: () -> Unit,
    onComments: () -> Unit, onShare: () -> Unit, onTag: (String) -> Unit,
) {
    Box(Modifier.fillMaxSize().clip(RoundedCornerShape(18.dp)).background(Color(0xFF292C25))) {
        if (post.videoUri != null) {
            NativeVideo(post.videoUri)
        } else {
            AsyncImage(
                model = photoUrl(post.image, 1000), contentDescription = post.caption,
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
            )
        }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x3320201D), Color.Transparent, Color(0xF010110E)))))
        Surface(
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
            color = Color(0x770E100D), shape = RoundedCornerShape(7.dp),
        ) {
            Text(if (post.videoUri == null) "FOR YOU" else "YOUR VIDEO", Modifier.padding(horizontal = 9.dp, vertical = 5.dp), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.7.sp)
        }
        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 9.dp, bottom = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            AsyncImage(photoUrl(post.avatar, 90), contentDescription = "${post.displayName} profile", modifier = Modifier.size(39.dp).clip(CircleShape).background(Color.White).padding(2.dp).clip(CircleShape))
            VideoAction(if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, if (isLiked) Coral else Color.White, formatCount(post.likes + if (isLiked) 1 else 0), onLike)
            VideoAction(Icons.Rounded.ChatBubbleOutline, Color.White, formatCount(post.comments), onComments)
            VideoAction(if (isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder, if (isSaved) Lime else Color.White, "Save", onSave)
            VideoAction(Icons.Rounded.Share, Color.White, "Share", onShare)
        }
        Column(
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(0.77f).padding(start = 14.dp, end = 7.dp, bottom = 19.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("@${post.handle}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Box(Modifier.size(15.dp).clip(CircleShape).background(Lime), contentAlignment = Alignment.Center) { Text("✓", color = Ink, fontSize = 9.sp, fontWeight = FontWeight.Black) }
                if (post.handle != "you" && !isFollowing) {
                    Surface(onClick = onFollow, color = Coral, shape = RoundedCornerShape(5.dp)) {
                        Text("Follow", Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(post.caption, color = Color.White, fontSize = 12.sp, lineHeight = 17.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                post.tags.take(2).forEach { tag -> Text("#$tag", Modifier.clickable { onTag(tag) }, color = Lime, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Text(post.sound, color = Color.White, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun VideoAction(icon: ImageVector, tint: Color, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick, modifier = Modifier.size(39.dp).shadow(1.dp, CircleShape).background(Color(0x3520211B), CircleShape)) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(23.dp))
        }
        Text(label, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun NativeVideo(uri: Uri) {
    val context = LocalContext.current
    val player = remember(uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = true
            prepare()
        }
    }
    DisposableEffect(player) { onDispose { player.release() } }
    AndroidView(
        factory = { viewContext -> PlayerView(viewContext).apply { this.player = player; useController = false; resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM } },
        update = { it.player = player },
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun ExploreScreen(search: String, onSearch: (String) -> Unit, onTag: (String) -> Unit, onLive: () -> Unit) {
    val topics = listOf("tinyjoys", "weekendreset", "easyfood", "outsideagain", "whatimreading", "desksetup", "cloudwatching", "homecooking")
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp)) {
        Text("Find your next favorite", fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
        Text("People, sounds, and small things worth a replay.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp, bottom = 15.dp))
        OutlinedTextField(value = search, onValueChange = onSearch, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Search CamTok", fontSize = 13.sp) }, leadingIcon = { Icon(Icons.Rounded.Search, null) }, shape = RoundedCornerShape(12.dp))
        SectionHeading("Topics picking up")
        topics.forEachIndexed { index, tag ->
            Row(Modifier.fillMaxWidth().clickable { onTag(tag) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(37.dp).clip(RoundedCornerShape(11.dp)).background(listOf(Lime, Color(0xFFFFD5C8), Color(0xFFC8E7F7), Color(0xFFE7D9FF))[index % 4]), contentAlignment = Alignment.Center) { Text("#", fontWeight = FontWeight.ExtraBold, color = Ink, fontSize = 19.sp) }
                Column(Modifier.weight(1f).padding(start = 11.dp)) { Text("#$tag", fontSize = 13.sp, fontWeight = FontWeight.Bold); Text("${(980 - index * 73)}K clips today", color = Muted, fontSize = 10.sp) }
                Icon(Icons.Rounded.PlayArrow, contentDescription = "Browse $tag", tint = Muted)
            }
            if (index != topics.lastIndex) Divider(color = Line, thickness = 1.dp)
        }
        SectionHeading("LIVE right now")
        Card(colors = CardDefaults.cardColors(containerColor = Ink), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().clickable(onClick = onLive)) {
            Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Videocam, contentDescription = null, tint = Lime, modifier = Modifier.size(30.dp))
                Column(Modifier.weight(1f).padding(horizontal = 11.dp)) { Text("Live rooms + PK matches", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp); Text("Preview rooms · broadcasting is not connected", color = Color(0xFFD0D2C8), fontSize = 10.sp) }
                Text("OPEN", color = Lime, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun InboxScreen(onMessage: () -> Unit) {
    val messages = listOf(
        Triple("milaoutside", "sent you a video you'll love", "2m"),
        Triple("juniper.jpeg", "wait til you see the last shot", "18m"),
        Triple("cookinwithkay", "thanks for the kind words!", "1h"),
        Triple("camTok team", "Welcome in. Your feed starts here.", "1d"),
    )
    Column(Modifier.fillMaxSize().padding(horizontal = 17.dp, vertical = 12.dp)) {
        Text("Inbox", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Text("Conversations worth having.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp, bottom = 12.dp))
        messages.forEachIndexed { index, item ->
            Row(Modifier.fillMaxWidth().clickable(onClick = onMessage).padding(vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                if (index < 3) AsyncImage(photoUrl(creators[index].avatar, 100), contentDescription = null, modifier = Modifier.size(45.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                else Box(Modifier.size(45.dp).clip(CircleShape).background(Lime), contentAlignment = Alignment.Center) { Text("C", fontWeight = FontWeight.Black) }
                Column(Modifier.weight(1f).padding(start = 11.dp)) {
                    Text(if (index < 3) "@${item.first}" else item.first, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(item.second, color = Muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(item.third, fontSize = 10.sp, color = Muted)
            }
            Divider(color = Line)
        }
        Spacer(Modifier.height(12.dp))
        Text("Your inbox is a preview", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text("Private messages need an account and secure messaging service.", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun ProfileScreen(onOpen: (CamPage) -> Unit, coinBalance: Int) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 17.dp, vertical = 10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Your corner", fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
            IconButton(onClick = { onOpen(CamPage.SETTINGS) }) { Icon(Icons.Rounded.Settings, contentDescription = "Settings") }
        }
        Card(Modifier.fillMaxWidth().padding(top = 6.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(15.dp)) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(photoUrl("photo-1534528741775-53994a69daeb", 160), contentDescription = "Profile portrait", modifier = Modifier.size(77.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                Text("Your profile", Modifier.padding(top = 9.dp), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text("@you · local demo account", color = Muted, fontSize = 11.sp)
                Row(Modifier.fillMaxWidth().padding(top = 15.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Metric("0", "Following")
                    Metric("0", "Followers")
                    Metric("0", "Likes")
                }
            }
        }
        SectionHeading("Creator tools")
        ProfileRow("Creator Studio", "Views, rewards, and post health", "CS") { onOpen(CamPage.CREATOR) }
        ProfileRow("Coins & wallet", "$coinBalance demo coins · no funds", "C") { onOpen(CamPage.WALLET) }
        ProfileRow("LIVE & matches", "Broadcast and PK setup", "LIVE") { onOpen(CamPage.LIVE) }
        SectionHeading("Your account")
        ProfileRow("Account information", "Profile, email, and devices", "ID") { onOpen(CamPage.SETTINGS) }
        ProfileRow("Privacy & safety", "Controls and reporting", "OK") { onOpen(CamPage.SETTINGS) }
        ProfileRow("Settings", "Notifications, accessibility, display", "SET") { onOpen(CamPage.SETTINGS) }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun CreatorScreen(onBack: () -> Unit, onPayout: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 17.dp, vertical = 10.dp)) {
        BackHeading("Creator Studio", onBack)
        Text("Your work, in one place.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp, bottom = 14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) { StatCard("0", "Video views", Modifier.weight(1f)); StatCard("$0.00", "Estimated rewards", Modifier.weight(1f)) }
        SectionHeading("Creator rewards")
        Card(colors = CardDefaults.cardColors(containerColor = Lime), shape = RoundedCornerShape(14.dp)) {
            Column(Modifier.padding(15.dp)) {
                Text("Build at your pace", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Text("Payouts remain disabled until account checks, tax onboarding, creator eligibility, and server-side fraud review are implemented.", fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp), lineHeight = 16.sp)
                TextButton(onClick = onPayout) { Text("Payout setup  ->", color = Ink, fontWeight = FontWeight.Bold) }
            }
        }
        SectionHeading("Post health")
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(13.dp)) {
            Column(Modifier.padding(14.dp)) {
                Text("Everything starts with a good post", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Your drafts and performance show up here once posts are uploaded to CamTok.", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp))
            }
        }
    }
}

@Composable
private fun WalletScreen(coinBalance: Int, onBack: () -> Unit, onPurchase: (Int) -> Unit, onPayout: () -> Unit) {
    val packs = listOf(70, 350, 700)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 17.dp, vertical = 10.dp)) {
        BackHeading("Coins & wallet", onBack)
        Card(Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = Ink), shape = RoundedCornerShape(15.dp)) {
            Column(Modifier.padding(18.dp)) {
                Text("CAMTOK COINS · DEMO", color = Lime, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, letterSpacing = 1.sp)
                Text("$coinBalance", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 4.dp))
                Text("Not real or redeemable", color = Color(0xFFC9CCC1), fontSize = 10.sp)
            }
        }
        SectionHeading("Coin packs")
        packs.forEach { pack ->
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("C", modifier = Modifier.size(36.dp).clip(CircleShape).background(Lime).padding(9.dp), fontWeight = FontWeight.ExtraBold)
                Column(Modifier.weight(1f).padding(start = 10.dp)) { Text("$pack coins", fontWeight = FontWeight.Bold, fontSize = 13.sp); Text("Sample pack", color = Muted, fontSize = 10.sp) }
                OutlinedButton(onClick = { onPurchase(pack) }, shape = RoundedCornerShape(8.dp)) { Text("Preview", fontSize = 11.sp) }
            }
        }
        SectionHeading("Creator payouts")
        Text("Planned payout providers", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text("JazzCash · Easypaisa · PayPal · Wise · bank transfer", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
        Button(onClick = onPayout, modifier = Modifier.fillMaxWidth().padding(top = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = Ink), shape = RoundedCornerShape(9.dp)) { Text("Payout eligibility", color = Color.White) }
        Text("Android digital goods require Google Play Billing. Payout rails are not connected. The displayed coins have no monetary value.", color = Muted, fontSize = 10.sp, lineHeight = 15.sp, modifier = Modifier.padding(top = 12.dp, bottom = 18.dp))
    }
}

@Composable
private fun LiveScreen(onBack: () -> Unit, onStart: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 17.dp, vertical = 10.dp)) {
        BackHeading("LIVE + PK", onBack)
        Text("A place to show up together.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, bottom = 14.dp))
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Coral), shape = RoundedCornerShape(10.dp)) { Icon(Icons.Rounded.Videocam, null); Spacer(Modifier.width(7.dp)); Text("Set up a LIVE") }
        SectionHeading("On the air · sample rooms")
        listOf("Kitchen table sessions", "Drawing tiny city maps", "Easy weekday noodles").forEachIndexed { index, title ->
            LiveRoom(title, listOf("@noahmakesmusic", "@juniper.jpeg", "@cookinwithkay")[index], "${2 + index * 3}.${index + 1}K watching") { onStart() }
        }
        SectionHeading("PK matches · preview")
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(13.dp)) {
            Column(Modifier.padding(14.dp)) {
                Text("Mila vs. Juniper", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Text("Friendly match · gifts and scores are disabled", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                OutlinedButton(onClick = onStart, modifier = Modifier.padding(top = 8.dp), shape = RoundedCornerShape(8.dp)) { Text("Match setup", fontSize = 11.sp) }
            }
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun LiveRoom(title: String, creator: String, viewers: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(bottom = 9.dp).clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(Coral))
            Column(Modifier.weight(1f).padding(start = 10.dp)) { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text("$creator · $viewers", color = Muted, fontSize = 10.sp) }
            Text("PREVIEW", color = Coral, fontWeight = FontWeight.ExtraBold, fontSize = 9.sp)
        }
    }
}

@Composable
private fun SettingsScreen(onBack: () -> Unit, onSafety: () -> Unit) {
    var notifications by remember { mutableStateOf(true) }
    var privateAccount by remember { mutableStateOf(false) }
    var dataSaver by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 17.dp, vertical = 10.dp)) {
        BackHeading("Settings", onBack)
        SectionHeading("Preferences")
        SettingToggle("Push notifications", "Likes, replies, and creators you follow", notifications) { notifications = it }
        SettingToggle("Private account", "New followers need your approval", privateAccount) { privateAccount = it }
        SettingToggle("Data saver", "Lower video quality on mobile data", dataSaver) { dataSaver = it }
        SectionHeading("Safety & account")
        ProfileRow("Safety center", "Reporting, blocks, and help", "SAFE", onSafety)
        ProfileRow("Account information", "Email, age, login methods", "ID", onSafety)
        ProfileRow("Your data", "Download or delete account data", "DATA", onSafety)
        SectionHeading("Integrity · server required")
        IntegrityStatus("Anti-bot & abuse detection", "Device signals, rate limits, and moderator review")
        IntegrityStatus("LIVE and PK anti-cheat", "Authoritative match state and server-verified scores")
        IntegrityStatus("Payment fraud checks", "Risk review before coin or creator-payout transactions")
        Card(Modifier.fillMaxWidth().padding(top = 14.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE5DE)), shape = RoundedCornerShape(12.dp)) {
            Text("Privacy and safety preferences here are a local preview. Account security and fraud protection must be enforced by the CamTok server.", Modifier.padding(13.dp), fontSize = 11.sp, lineHeight = 16.sp, color = Ink)
        }
    }
}

@Composable
private fun IntegrityStatus(title: String, detail: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Shield, contentDescription = null, tint = Coral, modifier = Modifier.size(21.dp))
        Column(Modifier.weight(1f).padding(start = 10.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(detail, color = Muted, fontSize = 10.sp, lineHeight = 14.sp)
        }
        Text("SERVER", color = Coral, fontWeight = FontWeight.ExtraBold, fontSize = 8.sp)
    }
}

@Composable
private fun SettingToggle(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text(subtitle, color = Muted, fontSize = 10.sp) }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun UploadDialog(selectedVideo: Uri?, onChooseVideo: () -> Unit, onRecordVideo: () -> Unit, onDismiss: () -> Unit, onPublish: (String, String) -> Unit) {
    var caption by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Make a little noise", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(colors = CardDefaults.cardColors(containerColor = if (selectedVideo == null) Paper else Color(0xFFEAF2D4)), shape = RoundedCornerShape(11.dp), modifier = Modifier.fillMaxWidth().clickable(onClick = onChooseVideo)) {
                    Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(if (selectedVideo == null) Icons.Rounded.Add else Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(25.dp), tint = Ink)
                        Text(if (selectedVideo == null) "Choose a video from this device" else "Video selected · tap to change", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("MP4 or WebM · stored locally in this preview", fontSize = 10.sp, color = Muted)
                    }
                }
                OutlinedButton(onClick = onRecordVideo, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(9.dp)) {
                    Icon(Icons.Rounded.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("Record with camera")
                }
                OutlinedTextField(value = caption, onValueChange = { if (it.length <= 150) caption = it }, label = { Text("Caption") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3)
                OutlinedTextField(value = topic, onValueChange = { if (it.length <= 24) topic = it }, label = { Text("Hashtag, optional") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = { onPublish(caption, topic) }, enabled = selectedVideo != null, colors = ButtonDefaults.buttonColors(containerColor = Ink), shape = RoundedCornerShape(8.dp)) { Text("Add to feed", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = Color.White,
    )
}

@Composable
private fun CommentsDialog(post: VideoPost, comments: List<String>, value: String, onValueChange: (String) -> Unit, onDismiss: () -> Unit, onSend: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Comments · ${formatCount(post.comments + comments.size)}", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(Modifier.heightIn(max = 340.dp)) {
                Text("@${post.handle}  ${post.caption}", fontSize = 11.sp, color = Muted)
                Spacer(Modifier.height(10.dp))
                Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    listOf("This made my whole morning", "Need the full story behind this one", "saving this for later").forEachIndexed { index, comment ->
                        Text("@${listOf("soraya", "joeymakesstuff", "tinydeskclub")[index]}  $comment", fontSize = 11.sp, lineHeight = 16.sp)
                    }
                    comments.forEach { Text("@you  $it", fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold) }
                }
                Row(Modifier.padding(top = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = value, onValueChange = onValueChange, modifier = Modifier.weight(1f), singleLine = true, placeholder = { Text("Add a thought...", fontSize = 11.sp) })
                    IconButton(onClick = onSend) { Icon(Icons.Rounded.PlayArrow, contentDescription = "Post comment", tint = Coral) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
        containerColor = Color.White,
    )
}

@Composable
private fun SectionHeading(title: String) {
    Text(title, modifier = Modifier.padding(top = 19.dp, bottom = 7.dp), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Ink)
}

@Composable
private fun ProfileRow(title: String, subtitle: String, badge: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(37.dp).clip(RoundedCornerShape(10.dp)).background(if (badge == "C") Lime else Color.White), contentAlignment = Alignment.Center) {
            Text(badge, fontSize = if (badge.length > 2) 8.sp else 11.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
        }
        Column(Modifier.weight(1f).padding(start = 11.dp)) { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp); Text(subtitle, color = Muted, fontSize = 10.sp) }
        Text("›", color = Muted, fontSize = 22.sp)
    }
}

@Composable
private fun Metric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp); Text(label, color = Muted, fontSize = 10.sp) }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(13.dp)) { Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp); Text(label, color = Muted, fontSize = 10.sp) }
    }
}

@Composable
private fun BackHeading(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) { Icon(Icons.Rounded.ArrowBack, contentDescription = "Back") }
        Text(title, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun EmptyMessage(title: String, body: String) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(title, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
        Text(body, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp))
    }
}

private fun formatCount(value: Int): String = when {
    value >= 1_000_000 -> "${value / 1_000_000}.${(value % 1_000_000) / 100_000}M"
    value >= 10_000 -> "${value / 1_000}K"
    value >= 1_000 -> "${value / 1_000}.${(value % 1_000) / 100}K"
    else -> value.toString()
}
