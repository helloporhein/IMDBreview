/*
 * Copyright (c) 2018 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.example.porwatch.search

import android.util.Log
import com.example.porwatch.model.RemoteDataSource
import com.example.porwatch.model.TmdbResponse
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.schedulers.Schedulers

class SearchPresenter(private var viewInterface: SearchContract.ViewInterface, private var dataSource: RemoteDataSource) : SearchContract.PresenterInterface {
  private val TAG = "SearchPresenter"
  private val compositeDisposable = CompositeDisposable()

  val searchResultsObservable: (String) -> Observable<TmdbResponse> = { query -> dataSource.searchResultsObservable(query) }

  val observer: DisposableObserver<TmdbResponse>
    get() = object : DisposableObserver<TmdbResponse>() {

      override fun onNext(@NonNull tmdbResponse: TmdbResponse) {
        Log.d(TAG, "OnNext" + tmdbResponse.totalResults)
        viewInterface.displayResult(tmdbResponse)
      }

      override fun onError(@NonNull e: Throwable) {
        Log.e(TAG,"Error fetching movie data.", e)
        viewInterface.displayError("Error fetching movie data.")
      }

      override fun onComplete() {
        Log.d(TAG, "Completed")
      }
    }

  override fun getSearchResults(query: String) {
    val searchResultsDisposable = searchResultsObservable(query)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(observer)

    compositeDisposable.add(searchResultsDisposable)
  }

  override fun stop() {
    compositeDisposable.clear()
  }
}