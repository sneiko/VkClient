package com.fdev.vkclient.crypto.prime

import android.content.Context
import com.fdev.vkclient.crypto.CryptoStorage
import com.fdev.vkclient.crypto.isPrime
import com.fdev.vkclient.lg.Lg
import com.fdev.vkclient.utils.applySchedulers
import com.fdev.vkclient.utils.getTime
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import java.math.BigInteger
import java.util.*

/**
 * Created by fuckyou on 12.12.2017.
 * generates safe prime for DH-2048
 */

class PrimeGeneratorCore(private val context: Context) {

    private val storage by lazy {
        CryptoStorage(context)
    }

    private val composite = CompositeDisposable()
    private var isCancelled = false

    fun run() {
        val storage = CryptoStorage(context)
        if (storage.isDefault() || storage.isObsolete()) {
            for (i in 1..2) {
                val s = Flowable.fromCallable { generate(i) }
                        .compose(applySchedulers())
                        .subscribe({
                            l("done")
                            isCancelled = true
                            composite.dispose()
                        }, {
                            lw("error occurred: $it")
                            it.printStackTrace()
                        })
                composite.add(s)
            }
        } else {
            l("already generated at ${getTime(storage.ts, withSeconds = true)}")
        }
    }

    private fun generate(threadNumber: Int) {
        l("start to generate custom primes", threadNumber)
        var q: BigInteger
        var p: BigInteger
        var bp: BigInteger
        val startTime = System.currentTimeMillis()
        var trie = 0
        do {
            p = BigInteger(BITS, 30, Random())
            bp = p.multiply(BigInteger.valueOf(2L)).add(BigInteger.ONE)
            q = p.subtract(BigInteger.ONE).divide(BigInteger.valueOf(2L))
            trie += 2
            if (trie % 10 == 0) {
                l("tries $trie", threadNumber)
            }
        } while (!isPrime(q) && !isPrime(bp) && !isCancelled)
        if (!isCancelled) {
            storage.prime = (if (isPrime(q)) p else bp).toString()
            l("found for ${(System.currentTimeMillis() - startTime) / 1000}s with $trie tries", threadNumber)
        } else {
            l("cancelled", threadNumber)
        }
    }

    private fun l(s: String, i: Int = 0) = Lg.i("[prime${if (i != 0) " $i" else ""}] $s")

    private fun lw(s: String) = Lg.wtf("[prime] $s")

    companion object {
        private const val BITS = 2048
    }
}