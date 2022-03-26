## Firestore datatype correspondence

| Firestore | Java                                         |
|-----------|----------------------------------------------|
| number    | java.lang.Long                               |
| string    | java.lang.String                             |
| boolean   | java.lang.Boolean                            |
| array     | java.util.ArrayList                          |
| map       | java.util.HashMap                            |
| timestamp | com.google.cloud.Timestamp / java.util.Date  |
| null      | null                                         |
| geo       | com.google.cloud.firestore.GeoPoint          |
| reference | com.google.cloud.firestore.DocumentReference |