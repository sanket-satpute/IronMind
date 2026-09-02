package com.sanket_satpute_20.ironmind.apps

data class AppClassificationSeed(
    val packageName: String,
    val category: AppCategory,
    val confidence: AppClassificationConfidence,
    val appName: String? = null
)

object DefaultAppClassifier {

    private val seeds = listOf(
        AppClassificationSeed("com.zhiliaoapp.musically", AppCategory.VOID, AppClassificationConfidence.HIGH, "TikTok"),
        AppClassificationSeed("com.instagram.android", AppCategory.VOID, AppClassificationConfidence.HIGH, "Instagram"),
        AppClassificationSeed("com.facebook.katana", AppCategory.VOID, AppClassificationConfidence.HIGH, "Facebook"),
        AppClassificationSeed("com.snapchat.android", AppCategory.VOID, AppClassificationConfidence.HIGH, "Snapchat"),
        AppClassificationSeed("com.reddit.frontpage", AppCategory.VOID, AppClassificationConfidence.HIGH, "Reddit"),
        AppClassificationSeed("com.twitter.android", AppCategory.VOID, AppClassificationConfidence.HIGH, "X"),
        AppClassificationSeed("com.instagram.threads", AppCategory.VOID, AppClassificationConfidence.HIGH, "Threads"),
        AppClassificationSeed("com.pinterest", AppCategory.VOID, AppClassificationConfidence.MEDIUM, "Pinterest"),
        AppClassificationSeed("com.lemon.lvoverseas", AppCategory.VOID, AppClassificationConfidence.MEDIUM, "CapCut"),
        AppClassificationSeed("com.temu", AppCategory.VOID, AppClassificationConfidence.MEDIUM, "Temu"),
        AppClassificationSeed("com.temu.buy", AppCategory.VOID, AppClassificationConfidence.MEDIUM, "Temu"),
        AppClassificationSeed("com.mxtech.videoplayer.ad", AppCategory.VOID, AppClassificationConfidence.MEDIUM, "MX Player"),
        AppClassificationSeed("com.mxtech.videoplayer.pro", AppCategory.VOID, AppClassificationConfidence.MEDIUM, "MX Player Pro"),
        AppClassificationSeed("com.amazon.mShop.android.shopping", AppCategory.VOID, AppClassificationConfidence.LOW, "Amazon Shopping"),

        AppClassificationSeed("com.whatsapp", AppCategory.SIGNAL, AppClassificationConfidence.HIGH, "WhatsApp"),
        AppClassificationSeed("com.whatsapp.w4b", AppCategory.SIGNAL, AppClassificationConfidence.HIGH, "WhatsApp Business"),
        AppClassificationSeed("org.telegram.messenger", AppCategory.SIGNAL, AppClassificationConfidence.HIGH, "Telegram"),
        AppClassificationSeed("org.thoughtcrime.securesms", AppCategory.SIGNAL, AppClassificationConfidence.HIGH, "Signal"),
        AppClassificationSeed("com.viber.voip", AppCategory.SIGNAL, AppClassificationConfidence.MEDIUM, "Viber"),
        AppClassificationSeed("jp.naver.line.android", AppCategory.SIGNAL, AppClassificationConfidence.MEDIUM, "Line"),
        AppClassificationSeed("com.google.android.apps.messaging", AppCategory.SIGNAL, AppClassificationConfidence.HIGH, "Google Messages"),
        AppClassificationSeed("com.facebook.orca", AppCategory.SIGNAL, AppClassificationConfidence.HIGH, "Messenger"),
        AppClassificationSeed("com.discord", AppCategory.SIGNAL, AppClassificationConfidence.MEDIUM, "Discord"),
        AppClassificationSeed("com.google.android.apps.googlevoice", AppCategory.SIGNAL, AppClassificationConfidence.MEDIUM, "Google Voice"),

        AppClassificationSeed("com.google.android.apps.maps", AppCategory.TOOL, AppClassificationConfidence.HIGH, "Google Maps"),
        AppClassificationSeed("com.google.android.calendar", AppCategory.TOOL, AppClassificationConfidence.HIGH, "Google Calendar"),
        AppClassificationSeed("com.google.android.apps.translate", AppCategory.TOOL, AppClassificationConfidence.MEDIUM, "Google Translate"),
        AppClassificationSeed("com.google.android.apps.files", AppCategory.TOOL, AppClassificationConfidence.MEDIUM, "Files by Google"),
        AppClassificationSeed("com.google.android.apps.nbu.files", AppCategory.TOOL, AppClassificationConfidence.MEDIUM, "Files by Google"),
        AppClassificationSeed("com.google.android.deskclock", AppCategory.TOOL, AppClassificationConfidence.MEDIUM, "Clock"),
        AppClassificationSeed("com.sec.android.app.clockpackage", AppCategory.TOOL, AppClassificationConfidence.LOW, "Clock"),
        AppClassificationSeed("com.google.android.GoogleCamera", AppCategory.TOOL, AppClassificationConfidence.LOW, "Camera"),
        AppClassificationSeed("com.android.camera2", AppCategory.TOOL, AppClassificationConfidence.LOW, "Camera"),
        AppClassificationSeed("com.android.settings", AppCategory.TOOL, AppClassificationConfidence.HIGH, "Settings"),
        AppClassificationSeed("com.miui.calculator", AppCategory.TOOL, AppClassificationConfidence.LOW, "Calculator"),
        AppClassificationSeed("com.google.android.calculator", AppCategory.TOOL, AppClassificationConfidence.MEDIUM, "Calculator"),

        AppClassificationSeed("com.android.chrome", AppCategory.CONTEXT, AppClassificationConfidence.HIGH, "Chrome"),
        AppClassificationSeed("org.mozilla.firefox", AppCategory.CONTEXT, AppClassificationConfidence.HIGH, "Firefox"),
        AppClassificationSeed("com.microsoft.emmx", AppCategory.CONTEXT, AppClassificationConfidence.HIGH, "Microsoft Edge"),
        AppClassificationSeed("com.brave.browser", AppCategory.CONTEXT, AppClassificationConfidence.HIGH, "Brave"),
        AppClassificationSeed("com.google.android.youtube", AppCategory.CONTEXT, AppClassificationConfidence.HIGH, "YouTube"),
        AppClassificationSeed("com.openai.chatgpt", AppCategory.CONTEXT, AppClassificationConfidence.HIGH, "ChatGPT"),
        AppClassificationSeed("com.google.android.apps.bard", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "Gemini"),
        AppClassificationSeed("com.google.android.gm", AppCategory.CONTEXT, AppClassificationConfidence.HIGH, "Gmail"),
        AppClassificationSeed("com.google.android.keep", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "Google Keep"),
        AppClassificationSeed("com.google.android.apps.tasks", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "Google Tasks"),
        AppClassificationSeed("com.google.android.apps.docs", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "Google Drive"),
        AppClassificationSeed("com.google.android.apps.docs.editors.docs", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "Google Docs"),
        AppClassificationSeed("com.google.android.apps.docs.editors.sheets", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "Google Sheets"),
        AppClassificationSeed("com.google.android.apps.docs.editors.slides", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "Google Slides"),
        AppClassificationSeed("com.microsoft.office.outlook", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "Outlook"),
        AppClassificationSeed("com.microsoft.teams", AppCategory.CONTEXT, AppClassificationConfidence.HIGH, "Teams"),
        AppClassificationSeed("com.Slack", AppCategory.CONTEXT, AppClassificationConfidence.HIGH, "Slack"),
        AppClassificationSeed("com.notion", AppCategory.CONTEXT, AppClassificationConfidence.HIGH, "Notion"),
        AppClassificationSeed("com.todoist", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "Todoist"),
        AppClassificationSeed("com.ticktick.task", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "TickTick"),
        AppClassificationSeed("com.spotify.music", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "Spotify"),
        AppClassificationSeed("com.netflix.mediaclient", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "Netflix"),
        AppClassificationSeed("com.amazon.avod.thirdpartyclient", AppCategory.CONTEXT, AppClassificationConfidence.LOW, "Prime Video"),
        AppClassificationSeed("in.startv.hotstar", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "Disney+ Hotstar"),
        AppClassificationSeed("com.disney.disneyplus", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "Disney+"),
        AppClassificationSeed("com.linkedin.android", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "LinkedIn"),
        AppClassificationSeed("org.khanacademy.android", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "Khan Academy"),
        AppClassificationSeed("org.coursera.android", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "Coursera"),
        AppClassificationSeed("com.udemy.android", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "Udemy"),
        AppClassificationSeed("com.duolingo", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "Duolingo"),
        AppClassificationSeed("com.calm.android", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "Calm"),
        AppClassificationSeed("com.getsomeheadspace.android", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "Headspace"),
        AppClassificationSeed("org.insighttimer", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "Insight Timer"),
        AppClassificationSeed("com.balance.app", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "Balance"),
        AppClassificationSeed("com.linkedin.corp.learning", AppCategory.CONTEXT, AppClassificationConfidence.MEDIUM, "LinkedIn Learning")
    )

    private val seedMap = seeds.associateBy { it.packageName }

    fun getSeed(packageName: String): AppClassificationSeed? = seedMap[packageName]

    fun getAllSeeds(): List<AppClassificationSeed> = seeds
}
