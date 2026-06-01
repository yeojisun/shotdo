<template>
  <div>
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
      
      <div class="user-profile">
        <div class="user-streak">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
            <path d="M17.66 11.57c-.77-.77-1.9-1.16-3.04-1.17l.55-2.73c.12-.62-.12-1.27-.6-1.68-.49-.41-1.16-.51-1.74-.26L6.68 8.43a2.002 2.002 0 0 0-1.18 1.8v8a2 2 0 0 0 2 2h7.32c.82 0 1.54-.5 1.84-1.26l2.36-6c.3-.77.13-1.64-.38-2.2zM8 18V11h2.21l-.54 2.7c-.12.6.02 1.22.4 1.7.38.48.97.76 1.6.76h3.69L13.7 18H8z"/>
          </svg>
          연속 {{ streak }}일 인증 중!
        </div>
      </div>
    </header>

    <!-- Main Grid -->
    <div class="app-grid">
      <!-- Calendar Card -->
      <section class="glass-panel calendar-card">
        <div class="calendar-header">
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

        <div class="calendar-grid">
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
              'status-has-todos': cell.hasTodos && !cell.isVerified
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

          <!-- To-do Input Form (only editable for today/future, but for simplicity editable anytime unless verified) -->
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
                오늘의 인증 성공!
              </div>
              <p style="font-size: 0.85rem; color: var(--text-secondary);">인증용 스냅샷이 성공적으로 저장되었습니다.</p>
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
                <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/>
                </svg>
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
                오늘의 할 일을 모두 완료해야 사진 인증이 가능합니다.
              </p>
            </div>
          </div>
        </div>
      </aside>
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
            다시 찍기
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
import { ref, computed, onMounted } from 'vue'

export default {
  name: 'App',
  setup() {
    // Current date values
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

    // Main App Data State (stored in localStorage)
    // Format: { 'YYYY-MM-DD': { verified: boolean, photo: string|null, todos: [ { id, text, completed } ] } }
    const history = ref({})

    const dayNames = ['일', '월', '화', '수', '목', '금', '토']

    // Helper functions
    const formatDateKey = (date) => {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }

    const loadData = () => {
      const savedData = localStorage.getItem('shotdo_history')
      if (savedData) {
        try {
          history.value = JSON.parse(savedData)
        } catch (e) {
          console.error('Failed to parse history data', e)
        }
      }
      
      // Populate with some initial dummy data if history is completely empty,
      // so the user can see what a populated calendar looks like!
      if (Object.keys(history.value).length === 0) {
        const dummyHistory = {}
        const samplePhoto1 = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="120" height="120" viewBox="0 0 100 100"><rect width="100" height="100" fill="%231a2333"/><circle cx="50" cy="50" r="30" fill="%2310b981" opacity="0.3"/><path d="M35 50 l10 10 l20 -20" stroke="%2310b981" stroke-width="6" fill="none" stroke-linecap="round"/></svg>'
        const samplePhoto2 = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="120" height="120" viewBox="0 0 100 100"><rect width="100" height="100" fill="%231a2333"/><rect x="25" y="25" width="50" height="50" rx="10" fill="%238b5cf6" opacity="0.3"/><path d="M35 50 l10 10 l20 -20" stroke="%238b5cf6" stroke-width="6" fill="none" stroke-linecap="round"/></svg>'
        
        // 2 days ago: Verified
        const date2DaysAgo = new Date()
        date2DaysAgo.setDate(today.getDate() - 2)
        dummyHistory[formatDateKey(date2DaysAgo)] = {
          verified: true,
          photo: samplePhoto1,
          todos: [
            { id: 1, text: '아침 조깅 30분', completed: true },
            { id: 2, text: '독서 1시간', completed: true }
          ]
        }

        // 1 day ago: Verified
        const date1DayAgo = new Date()
        date1DayAgo.setDate(today.getDate() - 1)
        dummyHistory[formatDateKey(date1DayAgo)] = {
          verified: true,
          photo: samplePhoto2,
          todos: [
            { id: 3, text: 'Spring Boot 강의 수강', completed: true },
            { id: 4, text: '알고리즘 문제 풀기', completed: true }
          ]
        }

        // Today: has unfinished todos
        dummyHistory[formatDateKey(today)] = {
          verified: false,
          photo: null,
          todos: [
            { id: 5, text: 'Vue.js 프론트엔드 퍼블리싱', completed: false },
            { id: 6, text: '포트폴리오 정리하기', completed: false }
          ]
        }

        history.value = dummyHistory
        saveData()
      }
      
      calculateStreak()
    }

    const saveData = () => {
      localStorage.setItem('shotdo_history', JSON.stringify(history.value))
    }

    const calculateStreak = () => {
      let currentStreak = 0
      let checkDate = new Date(today.getFullYear(), today.getMonth(), today.getDate())
      
      // If today is verified, we start counting. If not, check if yesterday was verified.
      const todayKey = formatDateKey(checkDate)
      const isTodayVerified = history.value[todayKey]?.verified
      
      if (!isTodayVerified) {
        checkDate.setDate(checkDate.getDate() - 1)
      }
      
      while (true) {
        const key = formatDateKey(checkDate)
        if (history.value[key]?.verified) {
          currentStreak++
          checkDate.setDate(checkDate.getDate() - 1)
        } else {
          break
        }
      }
      streak.value = currentStreak
    }

    // Selected state key
    const selectedDateKey = computed(() => formatDateKey(selectedDate.value))

    // Formatted Titles
    const formattedCurrentMonth = computed(() => {
      return currentMonth.value.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long' })
    })

    const formattedSelectedDateTitle = computed(() => {
      return selectedDate.value.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'short' })
    })

    // Selected date to-dos state
    const todosForSelectedDate = computed(() => {
      return history.value[selectedDateKey.value]?.todos || []
    })

    const isSelectedDateVerified = computed(() => {
      return history.value[selectedDateKey.value]?.verified || false
    })

    // Check if the user is allowed to verify today's list (only today, must have >0 todos, and all completed)
    const isVerificationAllowed = computed(() => {
      // Must be today
      const todayKey = formatDateKey(today)
      if (selectedDateKey.value !== todayKey) return false

      const dateData = history.value[selectedDateKey.value]
      if (!dateData || !dateData.todos || dateData.todos.length === 0) return false
      
      return dateData.todos.every(todo => todo.completed) && !dateData.verified
    })

    // To-dos counter for selected date
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
          isVerified: history.value[dStr]?.verified || false,
          hasTodos: (history.value[dStr]?.todos || []).length > 0,
          photo: history.value[dStr]?.photo || null
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
          isVerified: history.value[dStr]?.verified || false,
          hasTodos: (history.value[dStr]?.todos || []).length > 0,
          photo: history.value[dStr]?.photo || null
        })
      }
      
      // Buffer from next month to make it grid complete (6 rows total = 42 cells)
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
          isVerified: history.value[dStr]?.verified || false,
          hasTodos: (history.value[dStr]?.todos || []).length > 0,
          photo: history.value[dStr]?.photo || null
        })
      }
      
      return cells
    })

    // Calendar Operations
    const prevMonth = () => {
      currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() - 1, 1)
    }

    const nextMonth = () => {
      currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() + 1, 1)
    }

    const selectDate = (date) => {
      selectedDate.value = new Date(date.getFullYear(), date.getMonth(), date.getDate())
    }

    // To-do Operations
    const addTodo = () => {
      if (!newTodoText.value.trim() || isSelectedDateVerified.value) return
      
      const key = selectedDateKey.value
      if (!history.value[key]) {
        history.value[key] = {
          verified: false,
          photo: null,
          todos: []
        }
      }
      
      history.value[key].todos.push({
        id: Date.now(),
        text: newTodoText.value.trim(),
        completed: false
      })
      
      newTodoText.value = ''
      saveData()
    }

    const toggleTodo = (todoId) => {
      if (isSelectedDateVerified.value) return
      
      const key = selectedDateKey.value
      const todos = history.value[key]?.todos || []
      const todo = todos.find(t => t.id === todoId)
      if (todo) {
        todo.completed = !todo.completed
        saveData()
      }
    }

    const deleteTodo = (todoId) => {
      if (isSelectedDateVerified.value) return
      
      const key = selectedDateKey.value
      const todos = history.value[key]?.todos || []
      const index = todos.findIndex(t => t.id === todoId)
      if (index !== -1) {
        todos.splice(index, 1)
        
        // Cleanup empty day data
        if (todos.length === 0 && !history.value[key].verified) {
          delete history.value[key]
        }
        
        saveData()
      }
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
        
        // Wait for next tick so videoElement ref is rendered and bound
        setTimeout(() => {
          if (videoElement.value) {
            videoElement.value.srcObject = stream
          }
        }, 100)
      } catch (err) {
        console.warn('Webcam not accessible, falling back to file/simulation upload', err)
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
      
      // Draw mirror effect for front camera
      ctx.translate(canvas.width, 0)
      ctx.scale(-1, 1)
      ctx.drawImage(videoElement.value, 0, 0, canvas.width, canvas.height)
      
      // Reset scale
      ctx.setTransform(1, 0, 0, 1, 0, 0)
      
      // Add a watermark to look like a premium app authentication snap
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
      ctx.fillText('SHOTO DONE!', 60, 40)
      
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

    const submitVerification = () => {
      if (!capturedImage.value) return
      
      const key = selectedDateKey.value
      if (!history.value[key]) {
        history.value[key] = {
          verified: false,
          photo: null,
          todos: []
        }
      }
      
      history.value[key].verified = true
      history.value[key].photo = capturedImage.value
      
      saveData()
      calculateStreak()
      closeCameraModal()
    }

    onMounted(() => {
      loadData()
    })

    return {
      today,
      selectedDate,
      currentMonth,
      dayNames,
      calendarDays,
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
      formattedSelectedDateTitle,
      prevMonth,
      nextMonth,
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
      submitVerification
    }
  }
}
</script>
