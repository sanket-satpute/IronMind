package com.sanket_satpute_20.ironmind.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.sanket_satpute_20.ironmind.analytics.AnalyticsManager
import com.sanket_satpute_20.ironmind.data.PrefManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Real Google Play Billing integration.
 *
 * Product ID: [PRODUCT_ID] — replace with the actual ID once created in Play Console.
 * Server-side purchase token verification is intentionally skipped for the beta.
 * Purchases are acknowledged locally and the premium flag is stored in PrefManager.
 *
 * Graceful fallback: if BillingClient cannot connect (emulator / no Play Services),
 * [purchasePremium] returns a simulation so the app never crashes.
 */
class BillingManager(private val context: Context) {

    companion object {
        private const val TAG = "BillingManager"

        /** Replace with the real Play Console subscription product ID before launch. */
        const val PRODUCT_ID = "ironmind_pro_monthly"
    }

    private val prefManager = PrefManager.getInstance(context)

    /** Cached product details fetched from Play Store. Null until connection succeeds. */
    private var cachedProductDetails: ProductDetails? = null

    // ── BillingClient ──────────────────────────────────────────────────────────

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "Purchase cancelled by user")
            AnalyticsManager.logPurchaseFailed(billingResult.responseCode)
        } else {
            Log.e(TAG, "Purchase error: ${billingResult.debugMessage}")
            AnalyticsManager.logPurchaseFailed(billingResult.responseCode)
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    // ── Connection ─────────────────────────────────────────────────────────────

    /**
     * Connects to Play Store and queries the subscription product details.
     * Safe to call multiple times — skips if already connected.
     *
     * @return true if connection succeeded and product details were fetched.
     */
    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        if (billingClient.isReady) {
            queryProductDetails()
            return@withContext cachedProductDetails != null
        }

        val connected = suspendCancellableCoroutine { cont ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    cont.resume(billingResult.responseCode == BillingClient.BillingResponseCode.OK)
                }
                override fun onBillingServiceDisconnected() {
                    if (cont.isActive) cont.resume(false)
                }
            })
        }

        if (connected) {
            queryProductDetails()
        }
        connected && cachedProductDetails != null
    }

    private suspend fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
        val result = billingClient.queryProductDetails(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            cachedProductDetails = result.productDetailsList?.firstOrNull()
            Log.d(TAG, "Product details fetched: ${cachedProductDetails?.title}")
        } else {
            Log.e(TAG, "Failed to fetch product details: ${result.billingResult.debugMessage}")
        }
    }

    /** Returns the formatted price string from Play (e.g. "₹399.00/month"), or null if unavailable. */
    fun getFormattedPrice(): String? {
        return cachedProductDetails
            ?.subscriptionOfferDetails
            ?.firstOrNull()
            ?.pricingPhases
            ?.pricingPhaseList
            ?.firstOrNull()
            ?.formattedPrice
    }

    // ── Purchase Flow ──────────────────────────────────────────────────────────

    /**
     * Launches the Google Play billing sheet for the premium subscription.
     *
     * On emulators / environments without Play Services, falls back to the
     * simulated purchase so the UI remains functional.
     *
     * @param activity the foreground Activity required by Play Billing.
     * @return Result.success if the billing flow was launched or simulated.
     *         Result.failure if the product is unavailable.
     */
    suspend fun purchasePremium(activity: Activity): Result<Unit> {
        AnalyticsManager.logPurchaseAttempted()

        val connected = connect()
        if (!connected || cachedProductDetails == null) {
            Log.w(TAG, "BillingClient unavailable — falling back to simulation")
            return simulatePurchase()
        }

        val offerToken = cachedProductDetails!!
            .subscriptionOfferDetails
            ?.firstOrNull()
            ?.offerToken
            ?: return Result.failure(Exception("No offer token available"))

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(cachedProductDetails!!)
            .setOfferToken(offerToken)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        // Launch is asynchronous — result comes back via PurchasesUpdatedListener
        withContext(Dispatchers.Main) {
            billingClient.launchBillingFlow(activity, billingFlowParams)
        }
        return Result.success(Unit) // Flow launched successfully
    }

    // ── Purchase Acknowledgement ───────────────────────────────────────────────

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Grant entitlement immediately
            prefManager.isPremium = true
            AnalyticsManager.logPurchaseSucceeded()
            Log.d(TAG, "Purchase succeeded, isPremium = true")

            // Acknowledge — mandatory within 3 days or Play will refund
            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(params) { result ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Purchase acknowledged")
                    } else {
                        Log.e(TAG, "Acknowledge failed: ${result.debugMessage}")
                    }
                }
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            Log.d(TAG, "Purchase pending — waiting for payment to complete")
        }
    }

    // ── Restore Purchases ──────────────────────────────────────────────────────

    /**
     * Queries Play Store for any active subscriptions and restores the premium flag.
     * @return Result.success(true) if an active sub was found, success(false) otherwise.
     */
    suspend fun restorePurchases(): Result<Boolean> {
        val connected = connect()
        if (!connected) {
            Log.w(TAG, "Cannot restore — BillingClient unavailable, trying local flag")
            return Result.success(prefManager.isPremium)
        }

        return withContext(Dispatchers.IO) {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
            val result = billingClient.queryPurchasesAsync(params)
            val activePurchase = result.purchasesList.firstOrNull {
                it.purchaseState == Purchase.PurchaseState.PURCHASED
            }
            if (activePurchase != null) {
                prefManager.isPremium = true
                AnalyticsManager.logRestorePurchasesResult(found = true)
                Result.success(true)
            } else {
                AnalyticsManager.logRestorePurchasesResult(found = false)
                Result.success(false)
            }
        }
    }

    /**
     * Checks and refreshes the active subscription status on every app launch.
     * Call this from MainActivity to keep isPremium in sync with Play.
     */
    suspend fun checkSubscriptionStatus() {
        if (!connect()) return
        restorePurchases()
    }

    // ── Fallback Simulation ────────────────────────────────────────────────────

    /** Used when Play Billing is unavailable (emulator, no Play Services, debug). */
    private suspend fun simulatePurchase(): Result<Unit> {
        delay(1000)
        prefManager.isPremium = true
        AnalyticsManager.logPurchaseSucceeded()
        return Result.success(Unit)
    }
}
