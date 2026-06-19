package nl.nextlevelpilots.companion.network

object ApiConfig {

    const val PROD =
        "https://planning.nextlevelpilots.nl/api"

    const val DEV =
        "https://planning-dev.nextlevelpilots.nl/api"

    /** Production API — no runtime environment switch; dev URL is not used by release builds. */
    const val BASE_URL = PROD
}