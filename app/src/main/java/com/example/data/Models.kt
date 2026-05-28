package com.example.data

data class CoursePath(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val bannerGradientStart: String,
    val bannerGradientEnd: String,
    val lessons: List<Lesson>
)

data class Lesson(
    val id: String,
    val title: String,
    val durationMin: Int,
    val summary: String,
    val detailedExplanation: String,
    val codeExample: String,
    val quizQuestion: String,
    val quizOptions: List<String>,
    val quizAnswerIndex: Int
)

data class Challenge(
    val id: String,
    val title: String,
    val difficulty: String, // "Easy", "Medium", "Hard"
    val category: String, // "Array", "Loop", "Recursion", "Security"
    val description: String,
    val starterCode: String,
    val solutionKeyword: String, // Simulated compilation passes if code contains this keyword
    val testCaseInput: String,
    val testCaseExpectedOutput: String,
    val xpReward: Int,
    val emoji: String
)

data class UserProfile(
    val username: String = "GhostCoder",
    val email: String = "ghost@code.dev",
    val isLoggedIn: Boolean = false,
    val xp: Int = 1240,
    val level: Int = 14,
    val streakDays: Int = 12,
    val completedLessonIds: List<String> = listOf("py_vars"),
    val solvedChallengeIds: List<String> = listOf("spectral_loop"),
    val notificationsEnabled: Boolean = true,
    val bio: String = "Decrypting compiler errors since midnight. Standard code slinger.",
    val isSoundEffectsEnabled: Boolean = true
)

object StaticCoursesData {
    val paths = listOf(
        CoursePath(
            id = "python_basics",
            title = "Python Spectres",
            description = "Conquer dynamic scripting and spooky spectral loops in simple human terms.",
            emoji = "🐍",
            bannerGradientStart = "#8B5CF6", // Purple
            bannerGradientEnd = "#4F46E5",   // Indigo
            lessons = listOf(
                Lesson(
                    id = "py_vars",
                    title = "Ethereal Variables",
                    durationMin = 5,
                    summary = "Labeling values in Python without losing your soul.",
                    detailedExplanation = """
                        In Python, a variable is like a storage chest floating in space. You don't have to tell the computer what type of cargo is inside (called Dynamic Typing).
                        
                        You simply choose a name and use the assignment operator (=) to load your data chest:
                        
                        `ghost_count = 13`
                        
                        Rules for naming variables:
                        - Must start with a letter or underscore
                        - Case-sensitive (ghost vs GHOST are different!)
                        - No spaces! Use snake_case for multiple words.
                    """.trimIndent(),
                    codeExample = "ghosts_detected = 3\nghost_type = 'Poltergeist'\nprint(f'{ghosts_detected} {ghost_type} roaming!')",
                    quizQuestion = "Which variable declaration is valid and standard in Python?",
                    quizOptions = listOf("let num = 5", "ghost count = 12", "ghost_count = 12", "int ghost_count = 12"),
                    quizAnswerIndex = 2
                ),
                Lesson(
                    id = "py_loops",
                    title = "Spectral Loops",
                    durationMin = 8,
                    summary = "Master for-loops and while-loops to automate repetitive routines.",
                    detailedExplanation = """
                        Loops let you run a block of code multiple times.
                        In Python, the 'for' loop is incredibly powerful and reads like natural English:
                        
                        `for ghost in range(3):`
                        `    print("Boo!")`
                        
                        This prints 'Boo!' exactly three times (indexes 0, 1, 2).
                        
                        A 'while' loop runs as long as a condition is True:
                        `souls = 3`
                        `while souls > 0:`
                        `    print("Floating...")`
                        `    souls -= 1`
                    """.trimIndent(),
                    codeExample = "for i in range(1, 5):\n    print(f'Ghost #{i} has escaped!')",
                    quizQuestion = "What will range(3) yield in a Python list loop?",
                    quizOptions = listOf("[1, 2, 3]", "[0, 1, 2]", "[0, 1, 2, 3]", "An infinite loop"),
                    quizAnswerIndex = 1
                ),
                Lesson(
                    id = "py_funcs",
                    title = "Haunted Functions",
                    durationMin = 10,
                    summary = "Group reusable spell statements with the def keyword.",
                    detailedExplanation = """
                        A function is a reusable block of code that perform actions when invoked. Think of it as writing down a spell recipe once and casting it whenever you want.
                        
                        To define a function in Python, use 'def':
                        
                        `def cast_spell(spell_name):`
                        `    return f"Casting {spell_name}!"`
                        
                        Invoke it by typing:
                        `print(cast_spell("Invisibility"))`
                    """.trimIndent(),
                    codeExample = "def summon_ghost(name='Casper'):\n    return f'Ghost {name} is here!'\n\nprint(summon_ghost('Slimer'))",
                    quizQuestion = "What keyword is used to declare a function in Python?",
                    quizOptions = listOf("function", "void", "def", "func"),
                    quizAnswerIndex = 2
                )
            )
        ),
        CoursePath(
            id = "kotlin_sorcery",
            title = "Kotlin Sorcery",
            description = "Explore state-of-the-art native null safety and elegant type expressions.",
            emoji = "☕",
            bannerGradientStart = "#06B6D4", // Cyan
            bannerGradientEnd = "#0284C7",   // Dark cyan
            lessons = listOf(
                Lesson(
                    id = "kt_null",
                    title = "The Null Sentinel",
                    durationMin = 7,
                    summary = "Understand how Kotlin prevents the notorious Billion Dollar Mistake.",
                    detailedExplanation = """
                        A NullPointerException (NPE) occurs when you try to access a variable that is empty (null). Kotlin saves lives by requiring you to declare if a variable CAN be null.
                        
                        `var name: String = "Ghost"` // Cannot be null!
                        `var nameNullable: String? = null` // Can be null!
                        
                        To access a nullable variable safely, use the Safe Call operator (?.) or the Elvis operator (?:) for default values:
                        
                        `val len = nameNullable?.length ?: 0`
                    """.trimIndent(),
                    codeExample = "val spell: String? = null\nval description = spell?.uppercase() ?: \"No spell prepared\"\nprintln(description)",
                    quizQuestion = "How do you declare a variable that CAN hold a null value in Kotlin?",
                    quizOptions = listOf("var x: Nullable<Int>", "var x: Int?", "var x: nullable Int", "val x = null"),
                    quizAnswerIndex = 1
                ),
                Lesson(
                    id = "kt_lambdas",
                    title = "Lambda Whispers",
                    durationMin = 9,
                    summary = "Use functional parameters and trailing lambda notation elegantly.",
                    detailedExplanation = """
                        Lambdas are anonymous functions (functions without names) that can be passed as values.
                        In Kotlin, lambdas are wrapped in curly braces:
                        
                        `val sum = { a: Int, b: Int -> a + b }`
                        
                        If a lambda is the final parameter of a function, you can place it OUTSIDE the parenthesis! This is called Trailing Lambda notation and makes block structures gorgeous:
                        
                        `viewModel.loadData { items -> updateUI(items) }`
                    """.trimIndent(),
                    codeExample = "val doubleIt = { n: Int -> n * 2 }\nprintln(doubleIt(24)) // 48",
                    quizQuestion = "In Kotlin, layout parameters wrapping actions inside curly braces { } are called:",
                    quizOptions = listOf("Classes", "Lambdas", "Monads", "Anachronisms"),
                    quizAnswerIndex = 1
                )
            )
        ),
        CoursePath(
            id = "js_web",
            title = "Full-Stack Ghost",
            description = "Brew dynamic internet structures with Next.js and high contrast CSS.",
            emoji = "🌐",
            bannerGradientStart = "#F59E0B", // Orange/Amber
            bannerGradientEnd = "#D97706",
            lessons = listOf(
                Lesson(
                    id = "js_async",
                    title = "Ghost Promises (Async)",
                    durationMin = 8,
                    summary = "Fetch ethereal API data asynchronously using async/await.",
                    detailedExplanation = """
                        Web operations (like fetching code courses) take time. Instead of freezing the screen, JavaScript uses 'Promises'.
                        
                        Using 'async' and 'await' lets you write reactive async code that reads like synchronized step-by-step code:
                        
                        `async function summonData() {`
                        `    let data = await fetch('api/ghosts');`
                        `    let json = await data.json();`
                        `    return json;`
                        `}`
                    """.trimIndent(),
                    codeExample = "async function getSpookyQuote() {\n  const res = await fetch('https://api.spooky/quote');\n  return res.text();\n}",
                    quizQuestion = "Which keyword must accompany a function declaration to allow using the await keyword inside?",
                    quizOptions = listOf("promise", "async", "awaitable", "defer"),
                    quizAnswerIndex = 1
                )
            )
        )
    )

    val challenges = listOf(
        Challenge(
            id = "spectral_loop",
            title = "The Spectral Loop",
            difficulty = "Easy",
            category = "Loop",
            description = "Write a function `sum_ghosts(n)` that returns the sum of all integers from 1 to n (inclusive) simulating escaping spirits.",
            starterCode = "def sum_ghosts(n):\n    # Write your spectral logic below\n    pass # Complete me",
            solutionKeyword = "return n * (n + 1) // 2" , // Or a loop doing range
            testCaseInput = "sum_ghosts(10)",
            testCaseExpectedOutput = "55",
            xpReward = 250,
            emoji = "🧩"
        ),
        Challenge(
            id = "ethereal_security",
            title = "Ethereal Security",
            difficulty = "Hard",
            category = "Security",
            description = "Implement an algorithm `strip_phantom_nodes(nodes)` that takes in a string and deletes all occurrences of special characters '👻' and '💀' to secure the transmission.",
            starterCode = "def strip_phantom_nodes(data_str):\n    # Strip out standard poltergeist variables '👻' and '💀'\n    pass",
            solutionKeyword = "replace",
            testCaseInput = "strip_phantom_nodes('He👻ll💀o')",
            testCaseExpectedOutput = "'Hello'",
            xpReward = 1200,
            emoji = "🛡️"
        ),
        Challenge(
            id = "inverse_spectre",
            title = "Inverse Spectre",
            difficulty = "Medium",
            category = "Array",
            description = "Take an array of escape velocity strings and invert them so they are reversed.",
            starterCode = "def invert_spectre(arr):\n    # Reverses the order of the list elements.\n    pass",
            solutionKeyword = "[::-1]",
            testCaseInput = "invert_spectre([1, 2, 3])",
            testCaseExpectedOutput = "[3, 2, 1]",
            xpReward = 600,
            emoji = "🔮"
        )
    )
}
