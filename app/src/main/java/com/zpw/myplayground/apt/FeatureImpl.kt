package com.zpw.myplayground.apt

import com.zpw.annotation.Provided
import com.zpw.myplayground.apt.Feature

@Provided(Feature::class)
class FeatureImpl : Feature {
}