package ronancamargo

import ronancamargo.firestore.runners.RunnerSyntax

package object firestore {
  type JavaMap[K, V] = java.util.Map[K, V]

  object syntax {
    val runners: RunnerSyntax.type = RunnerSyntax
  }
}
