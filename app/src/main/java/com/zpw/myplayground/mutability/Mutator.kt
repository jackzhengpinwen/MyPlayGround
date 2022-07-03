package com.zpw.myplayground.mutability

import kotlin.reflect.KCallable
import kotlin.reflect.KProperty1
import kotlin.reflect.full.functions
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.valueParameters

/**
 * Mutates the specified property [p1] with provided [value]
 *
 * @receiver the type to mutate
 * @param p1 the property of [T] to mutate
 * @param value the value to assign
 * @return the mutated instance
 * @throws [IllegalArgumentException] if [T] is not a data class
 */
inline fun <reified T : Any, V> T.mutate(
    /**
     * KProperty1 表示一个属性，其上的操作将一个接收器作为参数。
        参数：
        T - 用于获取属性值的接收者的类型。
        V - 属性值的类型。
     */
    p1: KProperty1<T, V?>,
    /**
     * 闭包，给p1作为属性值赋值
     */
    value: T.() -> V?
): T {
    // 如果此类是数据类，则为 true。
    require(T::class.isData) {
        "${T::class.qualifiedName} is not a data class!"
    }

    // 返回在此类及其所有超类中声明的非扩展属性。
    val properties = T::class.memberProperties.map {
        it.name to it
    }.toMap()// 从给定的集合中返回一个包含所有键值对的新映射。返回的映射保留原始集合的条目迭代顺序。如果两对中的任何一对具有相同的key，则最后一个将添加到map中。

    // 获取copy方法的引用
    val copy = T::class.functions// 返回该类中声明的所有函数，包括该类和超类中声明的所有非静态方法，以及该类中声明的静态方法。
        .filterIsInstance<KCallable<T>>()// 返回一个列表，其中包含作为指定类型参数 R 的实例的所有元素。
        .single {// 返回匹配给定谓词的单个元素，如果没有或不止一个匹配元素，则抛出异常。
        it.name == "copy"
    }

    // 返回可以调用copy方法的实例
    val arg0 = mapOf(
        copy.instanceParameter!! to this// 返回一个参数，表示调用此可调用对象所需的此实例，如果此可调用对象不是类的成员，因此不采用此类参数，则返回 null。
    )

    // 获取到需要调用copy方法的实例
    val args = copy.valueParameters// 返回此可调用对象的参数，不包括此实例和扩展接收器参数。
        .map {
            it to when (it.name) {
                p1.name -> value.invoke(this)
                else -> properties[it.name]?.get(this)
            }
    }.toMap()

    // 使用参数到参数的指定映射调用此可调用对象并返回结果。如果在映射中找不到参数并且不是可选的（根据 KParameter.isOptional），
    // 或者其类型与提供的值的类型不匹配，则会引发异常。
    return copy.callBy(arg0 + args)
}

/**
 * Mutates the specified property [p1]::[p2] with provided [value]
 *
 * @receiver the type to mutate
 * @param p1 the property of [T] to mutate
 * @param p2 the property of [p1] to mutate
 * @param value the value to assign
 * @return the mutated instance
 * @throws [IllegalArgumentException] if [T] is not a data class
 */
inline fun <reified T : Any, reified A : Any, V> T.mutate(
        p1: KProperty1<T, A?>,
        p2: KProperty1<A, V?>,
        value: A.() -> V?
): T = this.mutate(p1) {
    p1.get(this)?.mutate(p2) {
        value.invoke(this)
    }
}

/**
 * Mutates the specified property [p1]::[p2]::[p3] with provided [value]
 *
 * @receiver the type to mutate
 * @param p1 the property of [T] to mutate
 * @param p2 the property of [p1] to mutate
 * @param p3 the property of [p2] to mutate
 * @param value the value to assign
 * @return the mutated instance
 * @throws [IllegalArgumentException] if [T] is not a data class
 */
inline fun <reified T : Any, reified A : Any, reified B : Any, V> T.mutate(
        p1: KProperty1<T, A?>,
        p2: KProperty1<A, B?>,
        p3: KProperty1<B, V?>,
        value: B.() -> V?
): T = this.mutate(p1) {
    p1.get(this)?.mutate(p2) {
        p2.get(this)?.mutate(p3) {
            value.invoke(this)
        }
    }
}

/**
 * Mutates the specified property [p1]::[p2]::[p3]::[p4] with provided [value]
 *
 * @receiver the type to mutate
 * @param p1 the property of [T] to mutate
 * @param p2 the property of [p1] to mutate
 * @param p3 the property of [p2] to mutate
 * @param p4 the property of [p3] to mutate
 * @param value the value to assign
 * @return the mutated instance
 * @throws [IllegalArgumentException] if [T] is not a data class
 */
inline fun <reified T : Any, reified A : Any, reified B : Any, reified C : Any, V> T.mutate(
        p1: KProperty1<T, A?>,
        p2: KProperty1<A, B?>,
        p3: KProperty1<B, C?>,
        p4: KProperty1<C, V?>,
        value: C.() -> V?
): T = this.mutate(p1) {
    p1.get(this)?.mutate(p2) {
        p2.get(this)?.mutate(p3) {
            p3.get(this)?.mutate(p4) {
                value.invoke(this)
            }
        }
    }
}

/**
 * Mutates the specified property [p1]::[p2]::[p3]::[p4]::[p5] with provided [value]
 *
 * @receiver the type to mutate
 * @param p1 the property of [T] to mutate
 * @param p2 the property of [p1] to mutate
 * @param p3 the property of [p2] to mutate
 * @param p4 the property of [p3] to mutate
 * @param p5 the property of [p4] to mutate
 * @param value the value to assign
 * @return the mutated instance
 * @throws [IllegalArgumentException] if [T] is not a data class
 */
inline fun <reified T : Any, reified A : Any, reified B : Any, reified C : Any, reified D : Any, V> T.mutate(
        p1: KProperty1<T, A?>,
        p2: KProperty1<A, B?>,
        p3: KProperty1<B, C?>,
        p4: KProperty1<C, D?>,
        p5: KProperty1<D, V?>,
        value: D.() -> V?
): T = this.mutate(p1) {
    p1.get(this)?.mutate(p2) {
        p2.get(this)?.mutate(p3) {
            p3.get(this)?.mutate(p4) {
                p4.get(this)?.mutate(p5) {
                    value.invoke(this)
                }
            }
        }
    }
}

/**
 * Mutates the specified property [p1]::[p2]::[p3]::[p4]::[p5]::[p6] with provided [value]
 *
 * @receiver the type to mutate
 * @param p1 the property of [T] to mutate
 * @param p2 the property of [p1] to mutate
 * @param p3 the property of [p2] to mutate
 * @param p4 the property of [p3] to mutate
 * @param p5 the property of [p4] to mutate
 * @param p6 the property of [p5] to mutate
 * @param value the value to assign
 * @return the mutated instance
 * @throws [IllegalArgumentException] if [T] is not a data class
 */
inline fun <reified T : Any, reified A : Any, reified B : Any, reified C : Any, reified D : Any, reified E : Any, V> T.mutate(
        p1: KProperty1<T, A?>,
        p2: KProperty1<A, B?>,
        p3: KProperty1<B, C?>,
        p4: KProperty1<C, D?>,
        p5: KProperty1<D, E?>,
        p6: KProperty1<E, V?>,
        value: E.() -> V?
): T = this.mutate(p1) {
    p1.get(this)?.mutate(p2) {
        p2.get(this)?.mutate(p3) {
            p3.get(this)?.mutate(p4) {
                p4.get(this)?.mutate(p5) {
                    p5.get(this)?.mutate(p6) {
                        value.invoke(this)
                    }
                }
            }
        }
    }
}

/**
 * Mutates the specified property [p1]::[p2]::[p3]::[p4]::[p5]::[p6]::[p7] with provided [value]
 *
 * @receiver the type to mutate
 * @param p1 the property of [T] to mutate
 * @param p2 the property of [p1] to mutate
 * @param p3 the property of [p2] to mutate
 * @param p4 the property of [p3] to mutate
 * @param p5 the property of [p4] to mutate
 * @param p6 the property of [p5] to mutate
 * @param p7 the property of [p6] to mutate
 * @param value the value to assign
 * @return the mutated instance
 * @throws [IllegalArgumentException] if [T] is not a data class
 */
inline fun <reified T : Any, reified A : Any, reified B : Any, reified C : Any, reified D : Any, reified E : Any, reified F : Any, V> T.mutate(
        p1: KProperty1<T, A?>,
        p2: KProperty1<A, B?>,
        p3: KProperty1<B, C?>,
        p4: KProperty1<C, D?>,
        p5: KProperty1<D, E?>,
        p6: KProperty1<E, F?>,
        p7: KProperty1<F, V?>,
        value: F.() -> V?
): T = this.mutate(p1) {
    p1.get(this)?.mutate(p2) {
        p2.get(this)?.mutate(p3) {
            p3.get(this)?.mutate(p4) {
                p4.get(this)?.mutate(p5) {
                    p5.get(this)?.mutate(p6) {
                        p6.get(this)?.mutate(p7) {
                            value.invoke(this)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Mutates the specified property [p1]::[p2]::[p3]::[p4]::[p5]::[p6]::[p7]::[p8] with provided [value]
 *
 * @receiver the type to mutate
 * @param p1 the property of [T] to mutate
 * @param p2 the property of [p1] to mutate
 * @param p3 the property of [p2] to mutate
 * @param p4 the property of [p3] to mutate
 * @param p5 the property of [p4] to mutate
 * @param p6 the property of [p5] to mutate
 * @param p7 the property of [p6] to mutate
 * @param p8 the property of [p7] to mutate
 * @param value the value to assign
 * @return the mutated instance
 * @throws [IllegalArgumentException] if [T] is not a data class
 */
inline fun <reified T : Any, reified A : Any, reified B : Any, reified C : Any, reified D : Any, reified E : Any, reified F : Any, reified G : Any, V> T.mutate(
        p1: KProperty1<T, A?>,
        p2: KProperty1<A, B?>,
        p3: KProperty1<B, C?>,
        p4: KProperty1<C, D?>,
        p5: KProperty1<D, E?>,
        p6: KProperty1<E, F?>,
        p7: KProperty1<F, G?>,
        p8: KProperty1<G, V?>,
        value: G.() -> V?
): T = this.mutate(p1) {
    p1.get(this)?.mutate(p2) {
        p2.get(this)?.mutate(p3) {
            p3.get(this)?.mutate(p4) {
                p4.get(this)?.mutate(p5) {
                    p5.get(this)?.mutate(p6) {
                        p6.get(this)?.mutate(p7) {
                            p7.get(this)?.mutate(p8) {
                                value.invoke(this)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Mutates the specified property [p1]::[p2]::[p3]::[p4]::[p5]::[p6]::[p7]::[p8]::[p9] with provided [value]
 *
 * @receiver the type to mutate
 * @param p1 the property of [T] to mutate
 * @param p2 the property of [p1] to mutate
 * @param p3 the property of [p2] to mutate
 * @param p4 the property of [p3] to mutate
 * @param p5 the property of [p4] to mutate
 * @param p6 the property of [p5] to mutate
 * @param p7 the property of [p6] to mutate
 * @param p8 the property of [p7] to mutate
 * @param p9 the property of [p8] to mutate
 * @param value the value to assign
 * @return the mutated instance
 * @throws [IllegalArgumentException] if [T] is not a data class
 */
inline fun <reified T : Any, reified A : Any, reified B : Any, reified C : Any, reified D : Any, reified E : Any, reified F : Any, reified G : Any, reified H : Any, V> T.mutate(
        p1: KProperty1<T, A?>,
        p2: KProperty1<A, B?>,
        p3: KProperty1<B, C?>,
        p4: KProperty1<C, D?>,
        p5: KProperty1<D, E?>,
        p6: KProperty1<E, F?>,
        p7: KProperty1<F, G?>,
        p8: KProperty1<G, H?>,
        p9: KProperty1<H, V?>,
        value: H.() -> V?
): T = this.mutate(p1) {
    p1.get(this)?.mutate(p2) {
        p2.get(this)?.mutate(p3) {
            p3.get(this)?.mutate(p4) {
                p4.get(this)?.mutate(p5) {
                    p5.get(this)?.mutate(p6) {
                        p6.get(this)?.mutate(p7) {
                            p7.get(this)?.mutate(p8) {
                                p8.get(this)?.mutate(p9) {
                                    value.invoke(this)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
