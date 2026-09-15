# Fix RichComponents
sed -i '/^package com.example.ui.common/d' app/src/main/java/com/example/ui/common/RichComponents.kt
sed -i '1i\
package com.example.ui.common
' app/src/main/java/com/example/ui/common/RichComponents.kt

# Fix ProfilesScreen
sed -i '/^package com.example.ui.profiles/d' app/src/main/java/com/example/ui/profiles/ProfilesScreen.kt
sed -i '1i\
package com.example.ui.profiles
' app/src/main/java/com/example/ui/profiles/ProfilesScreen.kt

# Fix ScheduleScreen
sed -i '/^package com.example.ui.schedule/d' app/src/main/java/com/example/ui/schedule/ScheduleScreen.kt
sed -i '1i\
package com.example.ui.schedule
' app/src/main/java/com/example/ui/schedule/ScheduleScreen.kt

# Fix HomeScreen
sed -i '/^package com.example.ui.home/d' app/src/main/java/com/example/ui/home/HomeScreen.kt
sed -i '1i\
package com.example.ui.home
' app/src/main/java/com/example/ui/home/HomeScreen.kt

# Fix ActiveSessionScreen
sed -i '/^package com.example.ui.home/d' app/src/main/java/com/example/ui/home/ActiveSessionScreen.kt
sed -i '1i\
package com.example.ui.home
' app/src/main/java/com/example/ui/home/ActiveSessionScreen.kt

