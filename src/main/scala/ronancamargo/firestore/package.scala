package ronancamargo

import ronancamargo.firestore.runners.RunnerSyntax

package object firestore {
  type JavaMap[K, V] = java.util.Map[K, V]
  type JavaList[A]   = java.util.List[A]

  object syntax {
    val runners: RunnerSyntax.type = RunnerSyntax
  }
}
