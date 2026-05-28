package com.example.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class GhostScreen {
    Splash,
    Login,
    Signup,
    Home,
    PathDetail,
    LessonDetail,
    Practice,
    Search,
    Profile,
    Notifications,
    Settings
}

data class ChatMessage(
    val id: String,
    val sender: String, // "user" or "ghost"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val isRead: Boolean = false,
    val type: String // "streak", "system", "reward"
)

class GhostViewModel : ViewModel() {

    // --- Screen State Control ---
    private val _currentScreen = MutableStateFlow(GhostScreen.Splash)
    val currentScreen: StateFlow<GhostScreen> = _currentScreen.asStateFlow()

    // Screen navigation stack history for simple back-press modeling
    private val screenHistory = mutableListOf<GhostScreen>()

    fun navigateTo(screen: GhostScreen) {
        if (_currentScreen.value != screen) {
            screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenHistory.isNotEmpty()) {
            _currentScreen.value = screenHistory.removeAt(screenHistory.size - 1)
            return true
        }
        return false
    }

    // --- Authentication State ---
    private val _profileState = MutableStateFlow(UserProfile())
    val profileState: StateFlow<UserProfile> = _profileState.asStateFlow()

    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")
    var signupUsername by mutableStateOf("")
    var signupEmail by mutableStateOf("")
    var signupPassword by mutableStateOf("")
    var authError by mutableStateOf<String?>(null)

    fun login() {
        if (loginEmail.isEmpty() || loginPassword.isEmpty()) {
            authError = "Please enter both email and password."
            return
        }
        if (!loginEmail.contains("@")) {
            authError = "Invalid email format."
            return
        }
        if (loginPassword.length < 4) {
            authError = "Password must be at least 4 characters."
            return
        }
        
        // Successful login
        authError = null
        _profileState.value = _profileState.value.copy(
            email = loginEmail,
            username = loginEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
            isLoggedIn = true
        )
        // Send a notification welcome
        addNotification(
            title = "A Spectral Hello!",
            message = "Welcome back, Ghost! Your streak has been retained. Ready to learn?",
            type = "system"
        )
        navigateTo(GhostScreen.Home)
    }

    fun signup() {
        if (signupUsername.trim().length < 3) {
            authError = "Username must be at least 3 characters."
            return
        }
        if (signupEmail.isEmpty() || signupPassword.isEmpty()) {
            authError = "Fields cannot be blank."
            return
        }
        if (!signupEmail.contains("@")) {
            authError = "Invalid email."
            return
        }
        
        authError = null
        _profileState.value = _profileState.value.copy(
            username = signupUsername,
            email = signupEmail,
            isLoggedIn = true,
            xp = 100, // Starts fresh with a warm-up bonus
            level = 1,
            streakDays = 1,
            completedLessonIds = emptyList(),
            solvedChallengeIds = emptyList()
        )
        addNotification(
            title = "Apprentice Initiated! 👻",
            message = "Welcome to Ghost Code. Start python, javascript or kotlin paths to earn daily XP!",
            type = "reward"
        )
        navigateTo(GhostScreen.Home)
    }

    fun logout() {
        _profileState.value = UserProfile(isLoggedIn = false)
        loginEmail = ""
        loginPassword = ""
        navigateTo(GhostScreen.Login)
    }

    // --- Catalog and Lesson Details Selection ---
    val coursePaths = StaticCoursesData.paths
    private val _selectedPath = MutableStateFlow<CoursePath>(StaticCoursesData.paths.first())
    val selectedPath: StateFlow<CoursePath> = _selectedPath.asStateFlow()

    private val _selectedLesson = MutableStateFlow<Lesson>(StaticCoursesData.paths.first().lessons.first())
    val selectedLesson: StateFlow<Lesson> = _selectedLesson.asStateFlow()

    fun selectPath(path: CoursePath) {
        _selectedPath.value = path
        navigateTo(GhostScreen.PathDetail)
    }

    fun selectLesson(lesson: Lesson) {
        _selectedLesson.value = lesson
        quizAnswerAttempt = null
        quizFeedback = null
        navigateTo(GhostScreen.LessonDetail)
    }

    // Interactive Lesson Quiz
    var quizAnswerAttempt by mutableStateOf<Int?>(null)
    var quizFeedback by mutableStateOf<String?>(null)

    fun submitQuizAnswer(selectedIndex: Int) {
        quizAnswerAttempt = selectedIndex
        val correctIndex = _selectedLesson.value.quizAnswerIndex
        if (selectedIndex == correctIndex) {
            quizFeedback = "🟢 Perfect! That's correct! +50 XP"
            val updatedLessons = _profileState.value.completedLessonIds.toMutableList()
            if (!updatedLessons.contains(_selectedLesson.value.id)) {
                updatedLessons.add(_selectedLesson.value.id)
                awardXp(50)
            }
        } else {
            quizFeedback = "🔴 Not quite. Think about the lesson summary and try again!"
        }
    }

    // --- LeetCode Challenge Practice Playground ---
    val challenges = StaticCoursesData.challenges
    private val _selectedChallenge = MutableStateFlow<Challenge>(StaticCoursesData.challenges.first())
    val selectedChallenge: StateFlow<Challenge> = _selectedChallenge.asStateFlow()

    var codingBuffer by mutableStateOf("")
    var terminalLogs by mutableStateOf("Ghost Shell initialized. Ready to compile...")
    var isCompiling by mutableStateOf(false)
    var hasSolvedCurrent by mutableStateOf(false)

    fun selectChallenge(challenge: Challenge) {
        _selectedChallenge.value = challenge
        codingBuffer = challenge.starterCode
        terminalLogs = "Ghost Shell initialized. Feed terminal test cases."
        isCompiling = false
        hasSolvedCurrent = _profileState.value.solvedChallengeIds.contains(challenge.id)
        navigateTo(GhostScreen.Practice)
    }

    fun runChallengeCode() {
        if (isCompiling) return
        isCompiling = true
        terminalLogs = "⚙️ Accessing spectra...\n👻 Parsing abstract syntax tree...\n👀 Injecting test cases...\n"
        
        viewModelScope.launch {
            kotlinx.coroutines.delay(1200) // Beautiful simulated loading lag
            val challenge = _selectedChallenge.value
            val passes = codingBuffer.contains(challenge.solutionKeyword) || codingBuffer.contains("return")
            
            if (passes) {
                terminalLogs += """
                    [COMPILE SUCCESS]
                    Input Evaluated: ${challenge.testCaseInput}
                    Expected Output: ${challenge.testCaseExpectedOutput}
                    Received Output: ${challenge.testCaseExpectedOutput}
                    
                    🌟 PERFECT SPEED! Program escaped without memory leaks!
                    🎉 Solved successfully! Earned +${challenge.xpReward} XP.
                """.trimIndent()
                
                val solvedList = _profileState.value.solvedChallengeIds.toMutableList()
                if (!solvedList.contains(challenge.id)) {
                    solvedList.add(challenge.id)
                    awardXp(challenge.xpReward)
                    _profileState.value = _profileState.value.copy(solvedChallengeIds = solvedList)
                    
                    addNotification(
                        title = "Challenge Decrypted! 🔥",
                        message = "Solved direct algorithm puzzle: '${challenge.title}'. Got +${challenge.xpReward} XP!",
                        type = "reward"
                    )
                }
                hasSolvedCurrent = true
            } else {
                terminalLogs += """
                    [ERROR: SYNTAX FAILURE]
                    Input Evaluated: ${challenge.testCaseInput}
                    AssertionError: Result did not match expected '${challenge.testCaseExpectedOutput}'.
                    
                    💡 Ghost Assist Tip: Ensure your code includes proper variable assignment or uses returns properly! Click 'Ask Ghost AI' below to get a breakdown of the solution.
                """.trimIndent()
            }
            isCompiling = false
        }
    }

    // --- Ask Ghost AI Chat Simulator ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage(
            id = "welcome_chat",
            sender = "ghost",
            text = "Welcome to the Ghost AI chamber! I can review code, explain spectral variables, loops, and functions. Ask me anything about programming!"
        )
    ))
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    var chatInput by mutableStateOf("")
    var isChatLoading by mutableStateOf(false)

    fun sendChatMessage() {
        val text = chatInput.trim()
        if (text.isEmpty() || isChatLoading) return

        val userMsg = ChatMessage(id = java.util.UUID.randomUUID().toString(), sender = "user", text = text)
        _chatMessages.value = _chatMessages.value + userMsg
        chatInput = ""
        isChatLoading = true

        viewModelScope.launch {
            // Retrieve from Gemini API helper
            val aiResponse = GeminiHelper.getExplanation(text)
            val ghostMsg = ChatMessage(id = java.util.UUID.randomUUID().toString(), sender = "ghost", text = aiResponse)
            _chatMessages.value = _chatMessages.value + ghostMsg
            isChatLoading = false
        }
    }

    fun requestAILessonExplanation(lessonTitle: String, lessonCode: String) {
        val prompt = "Compose a friendly, beginner-friendly, spooky 3-sentence explanation of code block below, related to '$lessonTitle'. Explain in plain, high-school terms with humor:\n\n$lessonCode"
        
        isChatLoading = true
        // Open the practice assistant screen (or profile chat context)
        // Add explaining placeholder first
        val requestMsg = ChatMessage(id = java.util.UUID.randomUUID().toString(), sender = "user", text = "Please explain the $lessonTitle lesson details in plain terms!")
        _chatMessages.value = _chatMessages.value + requestMsg
        
        viewModelScope.launch {
            val aiResponse = GeminiHelper.getExplanation(prompt)
            _chatMessages.value = _chatMessages.value + ChatMessage(
                id = java.util.UUID.randomUUID().toString(),
                sender = "ghost",
                text = aiResponse
            )
            isChatLoading = false
        }
    }

    // --- Search functionality ---
    var searchQuery by mutableStateOf("")
    var searchFilterByCategory by mutableStateOf("All") // "All", "Python", "Kotlin", "JS", "Easy", "Hard"

    fun performSearch(): SearchResults {
        val query = searchQuery.trim().lowercase()
        
        val matchedLessons = coursePaths.flatMap { path ->
            path.lessons.filter { lesson ->
                lesson.title.lowercase().contains(query) || lesson.summary.lowercase().contains(query)
            }.map { Pair(path, it) }
        }

        val matchedChallenges = challenges.filter { challenge ->
            challenge.title.lowercase().contains(query) || 
            challenge.description.lowercase().contains(query) ||
            challenge.category.lowercase().contains(query)
        }

        return SearchResults(matchedLessons, matchedChallenges)
    }

    data class SearchResults(
        val lessons: List<Pair<CoursePath, Lesson>>,
        val challenges: List<Challenge>
    )

    // --- Notifications section ---
    private val _notifications = MutableStateFlow<List<NotificationItem>>(listOf(
        NotificationItem(
            id = "nit1",
            title = "Daily Hunt Available 🧩",
            message = "Unlock high-value algorithms like 'The Spectral Loop'. +250 XP waiting!",
            timeAgo = "1 hour ago",
            type = "reward"
        ),
        NotificationItem(
            id = "nit2",
            title = "12-Day Streak Active! 🔥",
            message = "Keep learning daily to maintain your spectral fire. XP multipliers scale up at day 15!",
            timeAgo = "4 hours ago",
            type = "streak"
        ),
        NotificationItem(
            id = "nit3",
            title = "Dark Mode Optimization",
            message = "Immersive UI design activated perfectly. Contrast scales high for safe nocturnal spelling.",
            timeAgo = "1 day ago",
            isRead = true,
            type = "system"
        )
    ))
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    fun addNotification(title: String, message: String, type: String) {
        val newNotification = NotificationItem(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            message = message,
            timeAgo = "Just now",
            type = type
        )
        _notifications.value = listOf(newNotification) + _notifications.value
    }

    fun dismissNotification(id: String) {
        _notifications.value = _notifications.value.filter { it.id != id }
    }

    fun markAllAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    // --- Settings / Options state ---
    fun toggleSoundEffects() {
        val p = _profileState.value
        _profileState.value = p.copy(isSoundEffectsEnabled = !p.isSoundEffectsEnabled)
    }

    fun toggleNotifications() {
        val p = _profileState.value
        _profileState.value = p.copy(notificationsEnabled = !p.notificationsEnabled)
    }

    fun editBio(newBio: String) {
        _profileState.value = _profileState.value.copy(bio = newBio)
    }

    // --- XP Level Calculator ---
    private fun awardXp(amount: Int) {
        val current = _profileState.value
        val newXp = current.xp + amount
        // Calculation: Level = newXp / 100
        val targetLevel = (newXp / 100) + 1
        val levelUp = targetLevel > current.level
        
        _profileState.value = current.copy(
            xp = newXp,
            level = targetLevel
        )

        if (levelUp) {
            addNotification(
                title = "LEVEL UP INITIATED! ✨",
                message = "Huzzah! You have transcended code safety parameters. Now Level $targetLevel!",
                type = "reward"
            )
        }
    }
}
