<template>
  <div>
    <!-- Login / Signup Screen -->
    <div v-if="!token" class="login-wrapper">
      <div class="glass-panel login-card">
        <div class="logo" style="justify-content: center; margin-bottom: 2rem;">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24">
              <path d="M4 4h3l2-2h6l2 2h3a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2zm8 3a5 5 0 1 0 0 10 5 5 0 0 0 0-10zm0 2a3 3 0 1 1 0 6 3 3 0 0 1 0-6z"/>
            </svg>
          </div>
          <span class="logo-text">ShotDo</span>
        </div>

        <h2 style="text-align: center; margin-bottom: 1.5rem;">
          {{ isLoginMode ? '로그인' : '회원가입' }}
        </h2>

        <div v-if="authError" class="auth-error-banner">
          {{ authError }}
        </div>

        <form @submit.prevent="handleAuth" class="auth-form">
          <div class="form-group">
            <label>아이디</label>
            <input v-model="authUsername" type="text" class="todo-input" placeholder="아이디를 입력하세요" required />
          </div>

          <div class="form-group">
            <label>비밀번호</label>
            <input v-model="authPassword" type="password" class="todo-input" placeholder="비밀번호를 입력하세요" required />
          </div>

          <div v-if="!isLoginMode" class="form-group">
            <label>닉네임</label>
            <input v-model="authNickname" type="text" class="todo-input" placeholder="닉네임을 입력하세요" required />
          </div>

          <button type="submit" class="btn-large btn-mint" style="margin-top: 1rem;">
            {{ isLoginMode ? '로그인하기' : '가입하기' }}
          </button>
        </form>

        <div class="auth-toggle">
          <span>{{ isLoginMode ? '계정이 없으신가요?' : '이미 계정이 있으신가요?' }}</span>
          <button @click="toggleAuthMode" class="toggle-link-btn">
            {{ isLoginMode ? '회원가입' : '로그인' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Main App Dashboard -->
    <div v-else>
      <!-- Header -->
      <header class="glass-panel">
        <div class="logo">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24">
              <path d="M4 4h3l2-2h6l2 2h3a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2zm8 3a5 5 0 1 0 0 10 5 5 0 0 0 0-10zm0 2a3 3 0 1 1 0 6 3 3 0 0 1 0-6z"/>
            </svg>
          </div>
          <span class="logo-text">ShotDo</span>
        </div>

        <!-- View Mode Toggle -->
        <div class="view-toggle-container">
          <button 
            @click="viewMode = 'month'" 
            class="view-toggle-btn" 
            :class="{ active: viewMode === 'month' }"
          >
            월간 달력
          </button>
          <button 
            @click="viewMode = 'week'" 
            class="view-toggle-btn" 
            :class="{ active: viewMode === 'week' }"
          >
            주간 보드
          </button>
        </div>
        
        <div class="user-profile">
          <span class="user-name">{{ nickname }}님</span>
          <div class="user-streak">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M17.66 11.57c-.77-.77-1.9-1.16-3.04-1.17l.55-2.73c.12-.62-.12-1.27-.6-1.68-.49-.41-1.16-.51-1.74-.26L6.68 8.43a2.002 2.002 0 0 0-1.18 1.8v8a2 2 0 0 0 2 2h7.32c.82 0 1.54-.5 1.84-1.26l2.36-6c.3-.77.13-1.64-.38-2.2zM8 18V11h2.21l-.54 2.7c-.12.6.02 1.22.4 1.7.38.48.97.76 1.6.76h3.69L13.7 18H8z"/>
            </svg>
            연속 {{ streak }}일 인증 중!
          </div>
          <button @click="handleLogout" class="logout-btn">로그아웃</button>
        </div>
      </header>

      <!-- Main Grid -->
      <div class="app-grid">
        <!-- Calendar Card -->
        <section class="glass-panel calendar-card">
          <!-- Calendar Header (Month View) -->
          <div v-if="viewMode === 'month'" class="calendar-header">
            <button @click="prevMonth" class="calendar-nav-btn" aria-label="이전 달">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="15 18 9 12 15 6"></polyline>
              </svg>
            </button>
            <h2 class="calendar-month-year">{{ formattedCurrentMonth }}</h2>
            <button @click="nextMonth" class="calendar-nav-btn" aria-label="다음 달">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="9 18 15 12 9 6"></polyline>
              </svg>
            </button>
          </div>

          <!-- Weekly Header (Week View) -->
          <div v-else class="calendar-header">
            <button @click="prevWeek" class="calendar-nav-btn" aria-label="이전 주">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="15 18 9 12 15 6"></polyline>
              </svg>
            </button>
            <h2 class="calendar-month-year">{{ formattedCurrentWeek }}</h2>
            <button @click="nextWeek" class="calendar-nav-btn" aria-label="다음 주">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="9 18 15 12 9 6"></polyline>
              </svg>
            </button>
          </div>

          <!-- Month Grid -->
          <div v-if="viewMode === 'month'" class="calendar-grid">
            <!-- Day Headers -->
            <div v-for="dayName in dayNames" :key="dayName" class="day-header">
              {{ dayName }}
            </div>

            <!-- Day Cells -->
            <div 
              v-for="cell in calendarDays" 
              :key="cell.dateString"
              class="day-cell"
              :class="{ 
                'other-month': !cell.isCurrentMonth, 
                'today': cell.isToday,
                'status-verified': cell.isVerified,
                'status-has-todos': cell.hasTodos && !cell.isVerified,
                'selected-day': cell.dateString === selectedDateKey
              }"
              :style="cell.isVerified && cell.photo ? { background: `url(${cell.photo})` } : {}"
              @click="selectDate(cell.date)"
            >
              <span class="day-number">{{ cell.dayNumber }}</span>
              
              <!-- Success check mark badge on cell if verified -->
              <div v-if="cell.isVerified" class="day-success-badge">
                <svg viewBox="0 0 24 24">
                  <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"/>
                </svg>
              </div>
              
              <!-- Alert dot if date has todos but not verified yet -->
              <div v-else-if="cell.hasTodos" class="day-todo-dot"></div>
            </div>
          </div>

          <!-- Weekly Polaroid Board -->
          <div v-else class="weekly-board">
            <div 
              v-for="cell in weeklyDays" 
              :key="cell.dateString"
              class="weekly-day-card"
              :class="{ 
                'today': cell.isToday,
                'status-verified': cell.isVerified,
                'selected-day': cell.isSelected
              }"
              @click="selectDate(cell.date)"
            >
              <div class="weekly-card-header">
                <span class="weekly-day-name">{{ cell.dayName }}</span>
                <span class="weekly-date-num">{{ cell.dayNumber }}</span>
              </div>
              
              <div class="weekly-card-body">
                <!-- If photo verification exists -->
                <div v-if="cell.isVerified && cell.photo" class="weekly-photo-container">
                  <img :src="cell.photo" alt="인증샷" class="weekly-day-photo" />
                  <div class="weekly-success-badge">
                    <svg viewBox="0 0 24 24">
                      <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"/>
                    </svg>
                  </div>
                </div>
                
                <!-- Placeholder if no photo verification yet -->
                <div v-else class="weekly-empty-placeholder">
                  <div v-if="cell.hasTodos" class="weekly-todo-indicator">
                    <span class="pending-badge">Pending</span>
                  </div>
                  <div v-else class="weekly-empty-label">-</div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Sidebar Form and To-dos -->
        <aside class="sidebar-panel">
          <!-- Selected Date View & To-dos -->
          <div class="glass-panel todos-card">
            <p class="date-selected-title">{{ formattedSelectedDateTitle }}</p>
            <h2 style="margin-bottom: 0.5rem;">오늘의 할 일</h2>
            
            <div class="stats-container" style="margin-bottom: 1.5rem;">
              <div class="stat-item">
                <div class="stat-val">{{ activeTodosCount }}</div>
                <div class="stat-label">진행중</div>
              </div>
              <div class="stat-item">
                <div class="stat-val mint-color">{{ completedTodosCount }}</div>
                <div class="stat-label">완료됨</div>
              </div>
              <div class="stat-item">
                <div class="stat-val">{{ totalTodosCount }}</div>
                <div class="stat-label">전체</div>
              </div>
            </div>

            <!-- Progress Bar -->
            <div v-if="totalTodosCount > 0" style="margin-bottom: 1.5rem;">
              <div style="display: flex; justify-content: space-between; font-size: 0.85rem; color: var(--text-secondary);">
                <span>오늘의 달성률</span>
                <span>{{ completionPercentage }}%</span>
              </div>
              <div class="progress-container">
                <div class="progress-bar" :style="{ width: `${completionPercentage}%` }"></div>
              </div>
            </div>

            <!-- To-do Input Form -->
            <form v-if="!isSelectedDateVerified" @submit.prevent="addTodo" class="todo-input-form">
              <input 
                v-model="newTodoText"
                type="text" 
                placeholder="할 일을 등록하세요..." 
                class="todo-input"
                required
              />
              <button type="submit" class="todo-add-btn">추가</button>
            </form>

            <!-- To-do List -->
            <ul v-if="todosForSelectedDate.length > 0" class="todo-list">
              <li 
                v-for="todo in todosForSelectedDate" 
                :key="todo.id" 
                class="todo-item"
                :class="{ 'completed': todo.completed }"
              >
                <div class="todo-checkbox-wrapper" @click="toggleTodo(todo.id)">
                  <div class="custom-checkbox">
                    <svg viewBox="0 0 24 24">
                      <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"/>
                    </svg>
                  </div>
                  <span class="todo-text">{{ todo.text }}</span>
                </div>
                <button 
                  v-if="!isSelectedDateVerified" 
                  @click="deleteTodo(todo.id)" 
                  class="todo-delete-btn" 
                  title="삭제"
                >
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
                  </svg>
                </button>
              </li>
            </ul>

            <!-- Empty State -->
            <div v-else class="todo-empty-state">
              <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
              </svg>
              <p>할 일이 비어있습니다. 새로운 할 일을 추가해보세요!</p>
            </div>

            <!-- Certification Status Section -->
            <div class="certify-panel" style="margin-top: 2rem;">
              <!-- Already Verified -->
              <div v-if="isSelectedDateVerified" class="glass-panel" style="padding: 1.5rem; border-color: var(--color-mint); background: var(--color-mint-glow);">
                <div style="display: flex; align-items: center; justify-content: center; gap: 0.5rem; color: var(--color-mint); font-weight: 700; margin-bottom: 0.75rem;">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                  </svg>
                  인증 완료!
                </div>
                <p style="font-size: 0.85rem; color: var(--text-secondary);">인증용 스냅샷이 안전하게 업로드되었습니다.</p>
              </div>

              <!-- Ready to Certify -->
              <div v-else-if="isVerificationAllowed">
                <button @click="openCameraModal" class="btn-large btn-mint pulse-badge">
                  <svg viewBox="0 0 24 24">
                    <path d="M4 4h3l2-2h6l2 2h3a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2zm8 3a5 5 0 1 0 0 10 5 5 0 0 0 0-10zm0 2a3 3 0 1 1 0 6 3 3 0 0 1 0-6z"/>
                  </svg>
                  사진 촬영하여 하루 채우기
                </button>
                <p class="certify-tip">
                  모든 투두를 완료했습니다. 인증샷을 올려 하루를 채우세요!
                </p>
              </div>

              <!-- Blocked -->
              <div v-else>
                <button class="btn-large btn-disabled" disabled>
                  <svg viewBox="0 0 24 24">
                    <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"/>
                  </svg>
                  미인증 상태 (잠김)
                </button>
                <p class="certify-tip">
                  오늘의 할 일을 모두 완료해야 사진 인증이 가능합니다. (오늘 날짜만 가능)
                </p>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </div>

    <!-- Camera / File Auth Modal -->
    <div class="modal-overlay" :class="{ 'active': showCameraModal }">
      <div class="glass-panel modal-content">
        <div class="modal-header">
          <h2 style="font-size: 1.25rem;">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z"/>
            </svg>
            인증 사진 제출
          </h2>
          <button @click="closeCameraModal" class="modal-close-btn">&times;</button>
        </div>

        <div class="camera-preview-container">
          <!-- Webcam Video Stream -->
          <video 
            v-if="useWebcam && cameraStream && !capturedImage" 
            ref="videoElement" 
            autoplay 
            playsinline 
            class="camera-stream"
          ></video>

          <!-- Captured/Uploaded Image Preview -->
          <img 
            v-else-if="capturedImage" 
            :src="capturedImage" 
            class="photo-preview-image" 
            alt="인증샷 프리뷰"
          />

          <!-- Fallback when camera is not active or error -->
          <div v-else class="camera-fallback">
            <svg class="camera-fallback-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
            <p style="font-size: 0.9rem; color: var(--text-secondary);">카메라를 시작하거나 사진을 업로드하세요.</p>
            
            <div style="display: flex; gap: 0.5rem; margin-top: 0.5rem;">
              <button @click="startWebcam" class="file-upload-label" style="border-style: solid;">
                카메라 켜기
              </button>
              <label class="file-upload-label">
                파일 선택
                <input type="file" accept="image/*" @change="handleFileUpload" class="file-upload-input" />
              </label>
            </div>
          </div>
        </div>

        <div class="camera-actions">
          <!-- Close / Cancel -->
          <button @click="closeCameraModal" class="btn-large btn-disabled" style="flex: 1;">
            취소
          </button>
          
          <!-- Take Snapshot (Webcam mode) -->
          <button 
            v-if="useWebcam && cameraStream && !capturedImage" 
            @click="takeSnapshot" 
            class="btn-large btn-mint" 
            style="flex: 1;"
          >
            촬영하기
          </button>
          
          <!-- Re-take / Clear -->
          <button 
            v-else-if="capturedImage" 
            @click="retake" 
            class="btn-large btn-disabled" 
            style="flex: 1; border-color: var(--color-red); color: var(--color-red);"
          >
            다시 촬영
          </button>
          
          <!-- Submit -->
          <button 
            v-if="capturedImage" 
            @click="submitVerification" 
            class="btn-large btn-mint" 
            style="flex: 1;"
          >
            인증 제출
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'

export default {
  name: 'App',
  setup() {
    // API CONFIG
    // 로컬 Spring Boot 실행 주소. 실서버 배포 시 Beanstalk 주소로 교체 가능합니다.
    const API_BASE = 'https://shotdo-backend.onrender.com'

    // Auth States
    const token = ref(localStorage.getItem('shotdo_token') || null)
    const nickname = ref(localStorage.getItem('shotdo_nickname') || null)
    const isLoginMode = ref(true)
    const authUsername = ref('')
    const authPassword = ref('')
    const authNickname = ref('')
    const authError = ref('')

    // Current date states
    const today = new Date()
    const selectedDate = ref(new Date(today.getFullYear(), today.getMonth(), today.getDate()))
    const currentMonth = ref(new Date(today.getFullYear(), today.getMonth(), 1))

    // Form states
    const newTodoText = ref('')
    const showCameraModal = ref(false)
    const useWebcam = ref(false)
    const cameraStream = ref(null)
    const videoElement = ref(null)
    const capturedImage = ref(null)
    const streak = ref(0)

    // Calendar States loaded from API
    // Format: { 'YYYY-MM-DD': { hasTodos: boolean, verified: boolean, photoUrl: string|null } }
    const calendarStates = ref({})
    
    // View mode: 'month' or 'week'
    const viewMode = ref('month')

    // Selected date details
    const todosForSelectedDate = ref([])
    const isSelectedDateVerified = ref(false)
    const selectedDatePhoto = ref(null)

    const dayNames = ['일', '월', '화', '수', '목', '금', '토']

    // Date Format Helper
    const formatDateKey = (date) => {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }

    const selectedDateKey = computed(() => formatDateKey(selectedDate.value))

    // Auth Functions
    const toggleAuthMode = () => {
      isLoginMode.value = !isLoginMode.value
      authError.value = ''
      authUsername.value = ''
      authPassword.value = ''
      authNickname.value = ''
    }

    const handleAuth = async () => {
      authError.value = ''
      const url = isLoginMode.value ? `${API_BASE}/api/users/login` : `${API_BASE}/api/users/signup`
      const payload = isLoginMode.value 
        ? { username: authUsername.value, password: authPassword.value }
        : { username: authUsername.value, password: authPassword.value, nickname: authNickname.value }

      try {
        const res = await fetch(url, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        })

        const data = await res.json()

        if (!res.ok) {
          throw new Error(data.error || '인증에 실패했습니다.')
        }

        if (isLoginMode.value) {
          // Login Success
          token.value = data.token
          nickname.value = data.nickname
          localStorage.setItem('shotdo_token', data.token)
          localStorage.setItem('shotdo_nickname', data.nickname)
          
          await initDashboard()
        } else {
          // Signup Success -> Auto Login
          isLoginMode.value = true
          authPassword.value = ''
          authNickname.value = ''
          authError.value = '회원가입이 완료되었습니다. 로그인을 해주세요!'
        }
      } catch (e) {
        authError.value = e.message
      }
    }

    const handleLogout = () => {
      if (token.value) {
        fetch(`${API_BASE}/api/users/logout`, {
          method: 'POST',
          headers: { 'Authorization': token.value }
        }).catch(err => console.error('Logout error', err))
      }
      
      token.value = null
      nickname.value = null
      localStorage.removeItem('shotdo_token')
      localStorage.removeItem('shotdo_nickname')
      calendarStates.value = {}
      todosForSelectedDate.value = []
      isSelectedDateVerified.value = false
      selectedDatePhoto.value = null
      streak.value = 0
    }

    // API Integration Functions
    const fetchCalendarData = async () => {
      if (!token.value) return
      try {
        const days = viewMode.value === 'month' ? calendarDays.value : weeklyDays.value
        if (days.length === 0) return
        const start = days[0].dateString
        const end = days[days.length - 1].dateString

        const res = await fetch(`${API_BASE}/api/todos/calendar?startDate=${start}&endDate=${end}`, {
          headers: { 'Authorization': token.value }
        })
        
        if (res.ok) {
          calendarStates.value = await res.json()
          calculateStreak()
        } else if (res.status === 401) {
          handleLogout()
        }
      } catch (e) {
        console.error('Failed to fetch calendar data', e)
      }
    }

    const fetchDayDetails = async () => {
      if (!token.value) return
      try {
        const res = await fetch(`${API_BASE}/api/todos?date=${selectedDateKey.value}`, {
          headers: { 'Authorization': token.value }
        })
        if (res.ok) {
          const data = await res.json()
          todosForSelectedDate.value = data.todos
          isSelectedDateVerified.value = data.verified
          selectedDatePhoto.value = data.photoUrl
        }
      } catch (e) {
        console.error('Failed to fetch day details', e)
      }
    }

    const calculateStreak = () => {
      let currentStreak = 0
      let checkDate = new Date(today.getFullYear(), today.getMonth(), today.getDate())
      
      const todayKey = formatDateKey(checkDate)
      const isTodayVerified = calendarStates.value[todayKey]?.verified
      
      if (!isTodayVerified) {
        checkDate.setDate(checkDate.getDate() - 1)
      }
      
      while (true) {
        const key = formatDateKey(checkDate)
        if (calendarStates.value[key]?.verified) {
          currentStreak++
          checkDate.setDate(checkDate.getDate() - 1)
        } else {
          break
        }
      }
      streak.value = currentStreak
    }

    const addTodo = async () => {
      if (!newTodoText.value.trim() || isSelectedDateVerified.value) return
      
      try {
        const res = await fetch(`${API_BASE}/api/todos`, {
          method: 'POST',
          headers: { 
            'Content-Type': 'application/json',
            'Authorization': token.value
          },
          body: JSON.stringify({
            text: newTodoText.value.trim(),
            date: selectedDateKey.value
          })
        })

        if (res.ok) {
          newTodoText.value = ''
          await fetchDayDetails()
          await fetchCalendarData()
        } else {
          const errData = await res.json()
          alert(errData.error || '할 일을 추가하지 못했습니다.')
        }
      } catch (e) {
        console.error(e)
      }
    }

    const toggleTodo = async (todoId) => {
      if (isSelectedDateVerified.value) return
      try {
        const res = await fetch(`${API_BASE}/api/todos/${todoId}/toggle`, {
          method: 'PATCH',
          headers: { 'Authorization': token.value }
        })
        if (res.ok) {
          await fetchDayDetails()
        }
      } catch (e) {
        console.error(e)
      }
    }

    const deleteTodo = async (todoId) => {
      if (isSelectedDateVerified.value) return
      try {
        const res = await fetch(`${API_BASE}/api/todos/${todoId}`, {
          method: 'DELETE',
          headers: { 'Authorization': token.value }
        })
        if (res.ok) {
          await fetchDayDetails()
          await fetchCalendarData()
        }
      } catch (e) {
        console.error(e)
      }
    }

    const initDashboard = async () => {
      await fetchCalendarData()
      await fetchDayDetails()
    }

    // Calendar navigations watch
    watch(currentMonth, () => {
      fetchCalendarData()
    })

    watch(selectedDate, () => {
      fetchDayDetails()
      if (viewMode.value === 'week') {
        fetchCalendarData()
      }
    })

    watch(viewMode, () => {
      fetchCalendarData()
    })

    // Header formats
    const formattedCurrentMonth = computed(() => {
      return currentMonth.value.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long' })
    })

    const formattedCurrentWeek = computed(() => {
      const days = weeklyDays.value
      if (days.length === 0) return ''
      const start = days[0].date
      const end = days[6].date
      
      const startStr = start.toLocaleDateString('ko-KR', { year: 'numeric', month: 'numeric', day: 'numeric' })
      const endStr = end.toLocaleDateString('ko-KR', { month: 'numeric', day: 'numeric' })
      return `${startStr} ~ ${endStr}`
    })

    const formattedSelectedDateTitle = computed(() => {
      return selectedDate.value.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'short' })
    })

    // Verification Allow logic
    const isVerificationAllowed = computed(() => {
      const todayKey = formatDateKey(today)
      if (selectedDateKey.value !== todayKey) return false

      if (todosForSelectedDate.value.length === 0) return false
      return todosForSelectedDate.value.every(todo => todo.completed) && !isSelectedDateVerified.value
    })

    const totalTodosCount = computed(() => todosForSelectedDate.value.length)
    const completedTodosCount = computed(() => todosForSelectedDate.value.filter(t => t.completed).length)
    const activeTodosCount = computed(() => totalTodosCount.value - completedTodosCount.value)
    
    const completionPercentage = computed(() => {
      if (totalTodosCount.value === 0) return 0
      return Math.round((completedTodosCount.value / totalTodosCount.value) * 100)
    })

    // Calendar Days Generator
    const calendarDays = computed(() => {
      const year = currentMonth.value.getFullYear()
      const month = currentMonth.value.getMonth()
      
      const firstDayIndex = new Date(year, month, 1).getDay()
      const lastDay = new Date(year, month + 1, 0).getDate()
      const prevMonthLastDay = new Date(year, month, 0).getDate()
      
      const cells = []
      
      // Buffer from previous month
      for (let i = firstDayIndex - 1; i >= 0; i--) {
        const d = new Date(year, month - 1, prevMonthLastDay - i)
        const dStr = formatDateKey(d)
        cells.push({
          date: d,
          dateString: dStr,
          dayNumber: d.getDate(),
          isCurrentMonth: false,
          isToday: false,
          isVerified: calendarStates.value[dStr]?.verified || false,
          hasTodos: calendarStates.value[dStr]?.hasTodos || false,
          photo: calendarStates.value[dStr]?.photoUrl || null
        })
      }
      
      // Current month days
      const todayKey = formatDateKey(today)
      for (let i = 1; i <= lastDay; i++) {
        const d = new Date(year, month, i)
        const dStr = formatDateKey(d)
        cells.push({
          date: d,
          dateString: dStr,
          dayNumber: i,
          isCurrentMonth: true,
          isToday: dStr === todayKey,
          isVerified: calendarStates.value[dStr]?.verified || false,
          hasTodos: calendarStates.value[dStr]?.hasTodos || false,
          photo: calendarStates.value[dStr]?.photoUrl || null
        })
      }
      
      // Buffer from next month
      const remainingCells = 42 - cells.length
      for (let i = 1; i <= remainingCells; i++) {
        const d = new Date(year, month + 1, i)
        const dStr = formatDateKey(d)
        cells.push({
          date: d,
          dateString: dStr,
          dayNumber: i,
          isCurrentMonth: false,
          isToday: false,
          isVerified: calendarStates.value[dStr]?.verified || false,
          hasTodos: calendarStates.value[dStr]?.hasTodos || false,
          photo: calendarStates.value[dStr]?.photoUrl || null
        })
      }
      
      return cells
    })

    const isSameDay = (d1, d2) => {
      return d1.getFullYear() === d2.getFullYear() &&
             d1.getMonth() === d2.getMonth() &&
             d1.getDate() === d2.getDate()
    }

    const weeklyDays = computed(() => {
      const sel = new Date(selectedDate.value)
      const day = sel.getDay() // 0 is Sunday, 1 is Monday, etc.
      
      const diff = sel.getDate() - day
      const sunday = new Date(sel.getFullYear(), sel.getMonth(), diff)

      const cells = []
      const todayKey = formatDateKey(today)
      const selectedKey = selectedDateKey.value
      
      for (let i = 0; i < 7; i++) {
        const d = new Date(sunday)
        d.setDate(sunday.getDate() + i)
        
        const dStr = formatDateKey(d)
        cells.push({
          date: d,
          dateString: dStr,
          dayNumber: d.getDate(),
          dayName: dayNames[i],
          isToday: dStr === todayKey,
          isSelected: dStr === selectedKey,
          isVerified: calendarStates.value[dStr]?.verified || false,
          hasTodos: calendarStates.value[dStr]?.hasTodos || false,
          photo: calendarStates.value[dStr]?.photoUrl || null
        })
      }
      return cells
    })

    const prevMonth = () => {
      currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() - 1, 1)
    }

    const nextMonth = () => {
      currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() + 1, 1)
    }

    const prevWeek = () => {
      const d = new Date(selectedDate.value)
      d.setDate(d.getDate() - 7)
      selectedDate.value = d
      currentMonth.value = new Date(d.getFullYear(), d.getMonth(), 1)
    }

    const nextWeek = () => {
      const d = new Date(selectedDate.value)
      d.setDate(d.getDate() + 7)
      selectedDate.value = d
      currentMonth.value = new Date(d.getFullYear(), d.getMonth(), 1)
    }

    const selectDate = (date) => {
      selectedDate.value = new Date(date.getFullYear(), date.getMonth(), date.getDate())
    }

    // Camera / Auth Modal Operations
    const openCameraModal = () => {
      showCameraModal.value = true
      startWebcam()
    }

    const closeCameraModal = () => {
      stopWebcam()
      showCameraModal.value = false
      capturedImage.value = null
    }

    const startWebcam = async () => {
      useWebcam.value = true
      capturedImage.value = null
      
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ 
          video: { facingMode: 'user', width: 640, height: 480 } 
        })
        cameraStream.value = stream
        
        setTimeout(() => {
          if (videoElement.value) {
            videoElement.value.srcObject = stream
          }
        }, 100)
      } catch (err) {
        console.warn('Webcam not accessible, falling back to file upload', err)
        useWebcam.value = false
        cameraStream.value = null
      }
    }

    const stopWebcam = () => {
      if (cameraStream.value) {
        cameraStream.value.getTracks().forEach(track => track.stop())
        cameraStream.value = null
      }
    }

    const takeSnapshot = () => {
      if (!videoElement.value) return
      
      const canvas = document.createElement('canvas')
      canvas.width = videoElement.value.videoWidth || 640
      canvas.height = videoElement.value.videoHeight || 480
      const ctx = canvas.getContext('2d')
      
      ctx.translate(canvas.width, 0)
      ctx.scale(-1, 1)
      ctx.drawImage(videoElement.value, 0, 0, canvas.width, canvas.height)
      ctx.setTransform(1, 0, 0, 1, 0, 0)
      
      // Watermark
      ctx.fillStyle = 'rgba(16, 185, 129, 0.8)'
      ctx.beginPath()
      ctx.arc(35, 35, 15, 0, 2 * Math.PI)
      ctx.fill()
      
      ctx.strokeStyle = '#ffffff'
      ctx.lineWidth = 3
      ctx.beginPath()
      ctx.moveTo(28, 35)
      ctx.lineTo(33, 40)
      ctx.lineTo(42, 30)
      ctx.stroke()

      ctx.fillStyle = '#ffffff'
      ctx.font = 'bold 16px Outfit, sans-serif'
      ctx.fillText('SHOTDO DONE!', 60, 40)
      
      capturedImage.value = canvas.toDataURL('image/jpeg')
      stopWebcam()
    }

    const handleFileUpload = (event) => {
      const file = event.target.files[0]
      if (file) {
        const reader = new FileReader()
        reader.onload = (e) => {
          capturedImage.value = e.target.result
        }
        reader.readAsDataURL(file)
      }
    }

    const retake = () => {
      capturedImage.value = null
      startWebcam()
    }

    // Convert Base64 DataUrl to Binary Blob for HTTP uploading
    const dataURItoBlob = (dataURI) => {
      const byteString = atob(dataURI.split(',')[1])
      const mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0]
      const ab = new ArrayBuffer(byteString.length)
      const ia = new Uint8Array(ab)
      for (let i = 0; i < byteString.length; i++) {
        ia[i] = byteString.charCodeAt(i)
      }
      return new Blob([ab], { type: mimeString })
    }

    // SUBMIT AUTHENTICATION (AWS S3 Pre-signed URL flow)
    const submitVerification = async () => {
      if (!capturedImage.value) return
      
      try {
        // 1. Get S3 Pre-signed URL from Spring Boot
        const presignedRes = await fetch(`${API_BASE}/api/todos/presigned-url?extension=jpg`, {
          headers: { 'Authorization': token.value }
        })
        
        if (!presignedRes.ok) throw new Error('S3 업로드 URL 발급 실패')
        const presignedData = await presignedRes.json()
        const uploadUrl = presignedData.uploadUrl
        
        // 2. Upload image binary directly to AWS S3
        const imageBlob = dataURItoBlob(capturedImage.value)
        const s3UploadRes = await fetch(uploadUrl, {
          method: 'PUT',
          headers: { 'Content-Type': 'image/jpeg' },
          body: imageBlob
        })
        
        if (!s3UploadRes.ok) throw new Error('S3 사진 전송 실패')
        
        // S3 public image URL (strip the query parameters from presigned url)
        const cleanS3ImageUrl = uploadUrl.split('?')[0]
        
        // 3. Confirm verification to Backend DB
        const verifyRes = await fetch(`${API_BASE}/api/todos/verify`, {
          method: 'POST',
          headers: { 
            'Content-Type': 'application/json',
            'Authorization': token.value 
          },
          body: JSON.stringify({
            date: selectedDateKey.value,
            photoUrl: cleanS3ImageUrl
          })
        })
        
        if (verifyRes.ok) {
          await fetchDayDetails()
          await fetchCalendarData()
          closeCameraModal()
        } else {
          const errData = await verifyRes.json()
          alert(errData.error || '인증 등록 실패')
        }
      } catch (e) {
        console.error(e)
        alert(`인증 처리 중 오류가 발생했습니다: ${e.message}`)
      }
    }

    onMounted(() => {
      if (token.value) {
        initDashboard()
      }
    })

    return {
      today,
      selectedDate,
      currentMonth,
      dayNames,
      calendarDays,
      weeklyDays,
      viewMode,
      todosForSelectedDate,
      totalTodosCount,
      completedTodosCount,
      activeTodosCount,
      completionPercentage,
      isSelectedDateVerified,
      isVerificationAllowed,
      newTodoText,
      showCameraModal,
      useWebcam,
      cameraStream,
      videoElement,
      capturedImage,
      streak,
      formattedCurrentMonth,
      formattedCurrentWeek,
      formattedSelectedDateTitle,
      prevMonth,
      nextMonth,
      prevWeek,
      nextWeek,
      selectDate,
      addTodo,
      toggleTodo,
      deleteTodo,
      openCameraModal,
      closeCameraModal,
      startWebcam,
      takeSnapshot,
      handleFileUpload,
      retake,
      submitVerification,
      
      // Auth Setup
      token,
      nickname,
      isLoginMode,
      authUsername,
      authPassword,
      authNickname,
      authError,
      toggleAuthMode,
      handleAuth,
      handleLogout
    }
  }
}
</script>
