import { useEffect, useState } from 'react'
import './App.css'

type StudyTopic = {
  id: number
  title: string
  category: string
  description: string
  niceToHave: boolean
}

type TopicsState =
  | { status: 'loading' }
  | { status: 'success'; topics: StudyTopic[] }
  | { status: 'error' }

type StudySession = {
  id: number
  startTime: string
  endTime: string | null
  status: 'STARTED' | 'COMPLETED' | 'CANCELLED'
  studyTopic: StudyTopic
}

type SessionState =
  | { status: 'loading' }
  | { status: 'success'; session: StudySession | null }
  | { status: 'error' }

async function fetchActiveSession(signal?: AbortSignal): Promise<StudySession | null> {
  const response = await fetch('/api/sessions/active', { signal })
  if (response.status === 204) return null
  if (response.status !== 200) throw new Error('Could not load active session')
  return response.json()
}

function App() {
  const [state, setState] = useState<TopicsState>({ status: 'loading' })
  const [attempt, setAttempt] = useState(0)
  const [sessionState, setSessionState] = useState<SessionState>({ status: 'loading' })
  const [sessionAttempt, setSessionAttempt] = useState(0)
  const [startingTopicId, setStartingTopicId] = useState<number | null>(null)
  const [startError, setStartError] = useState<string | null>(null)
  const [completingSession, setCompletingSession] = useState(false)
  const [completionError, setCompletionError] = useState<string | null>(null)
  const [completionMessage, setCompletionMessage] = useState<string | null>(null)

  useEffect(() => {
    const controller = new AbortController()
    void fetchActiveSession(controller.signal)
      .then(session => {
        if (!controller.signal.aborted) setSessionState({ status: 'success', session })
      })
      .catch(() => {
        if (!controller.signal.aborted) setSessionState({ status: 'error' })
      })
    return () => controller.abort()
  }, [sessionAttempt])

  function reloadSession() {
    setSessionState({ status: 'loading' })
    setSessionAttempt(value => value + 1)
  }

  async function startSession(topicId: number) {
    if (startingTopicId !== null || completingSession || sessionState.status !== 'success' || sessionState.session) return
    setStartingTopicId(topicId)
    setStartError(null)
    setCompletionError(null)
    setCompletionMessage(null)
    try {
      const response = await fetch('/api/sessions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ topicId }),
      })
      if (response.status === 409) {
        setStartError('Занятие уже запущено. Обновляем его состояние.')
        reloadSession()
        return
      }
      if (response.status === 404) {
        setStartError('Тема больше не существует. Обнови список тем.')
        return
      }
      if (response.status !== 201) throw new Error('Could not start session')
      const session: StudySession = await response.json()
      setSessionState({ status: 'success', session })
    } catch {
      setStartError('Не удалось подтвердить запуск занятия. Проверь состояние перед повторной попыткой.')
      // The server may have saved the session even if its response was lost.
      reloadSession()
    } finally {
      setStartingTopicId(null)
    }
  }

  async function completeSession() {
    if (completingSession || startingTopicId !== null || sessionState.status !== 'success' || !sessionState.session) return
    const sessionId = sessionState.session.id
    setCompletingSession(true)
    setCompletionError(null)
    setCompletionMessage(null)
    setStartError(null)
    try {
      const response = await fetch(`/api/sessions/${sessionId}/complete`, { method: 'POST' })
      if (response.status === 404 || response.status === 409) {
        setCompletionError(response.status === 404
          ? 'Занятие больше не найдено. Обновляем текущее состояние.'
          : 'Состояние занятия уже изменилось. Обновляем текущее состояние.')
        return
      }
      if (response.status !== 200) throw new Error('Could not complete session')
      setCompletionMessage('Занятие завершено.')
    } catch {
      setCompletionError('Не удалось подтвердить завершение занятия. Проверяем его состояние перед повторной попыткой.')
    } finally {
      // Reconcile even when the response is lost: the server may have completed the session.
      reloadSession()
      setCompletingSession(false)
    }
  }

  const activeSession = sessionState.status === 'success' ? sessionState.session : null
  const cannotStart = sessionState.status !== 'success' || activeSession !== null || startingTopicId !== null || completingSession

  useEffect(() => {
    const controller = new AbortController()

    async function loadTopics() {
      try {
        const response = await fetch('/api/topics', { signal: controller.signal })
        if (!response.ok) throw new Error('Could not load topics')
        const topics: StudyTopic[] = await response.json()
        if (!controller.signal.aborted) setState({ status: 'success', topics })
      } catch {
        if (!controller.signal.aborted) setState({ status: 'error' })
      }
    }

    void loadTopics()
    return () => controller.abort()
  }, [attempt])

  function reload() {
    setState({ status: 'loading' })
    setAttempt(value => value + 1)
  }

  return (
    <div className="app">
      <header className="topbar">
        <a className="brand" href="/">Study Tracker<span>Lead Backend Developer</span></a>
        <span className="assessment">Аттестация · 29 января 2027</span>
      </header>

      <main>
        <div className="page-heading">
          <div>
            <p className="eyebrow">Программа подготовки</p>
            <h1>Учебные темы</h1>
            <p className="intro">Компетенции из требований к аттестации. Начни с одной темы.</p>
          </div>
          <button onClick={reload} disabled={state.status === 'loading'}>
            {state.status === 'loading' ? 'Загрузка…' : 'Обновить'}
          </button>
        </div>

        <section className="session-panel notice" aria-labelledby="session-heading" aria-busy={sessionState.status === 'loading' || completingSession}>
          <div className="session-heading">
            <h2 id="session-heading">Текущее занятие</h2>
            <button onClick={reloadSession} disabled={sessionState.status === 'loading' || startingTopicId !== null || completingSession}>Обновить занятие</button>
          </div>
          <div aria-live="polite">
            {sessionState.status === 'loading' && <p role="status">Проверяем активное занятие…</p>}
            {sessionState.status === 'error' && <p role="alert">Не удалось загрузить занятие. Нажми «Обновить занятие», чтобы повторить проверку.</p>}
            {sessionState.status === 'success' && !activeSession && <p>Нет активного занятия. Выбери тему ниже, чтобы начать.</p>}
            {activeSession && <>
              <p className="session-topic">{activeSession.studyTopic.title}</p>
              <p><span className="badge">В процессе · {activeSession.status}</span></p>
              <p className="session-time">Начало: <time dateTime={activeSession.startTime}>{new Date(activeSession.startTime).toLocaleString('ru-RU')}</time></p>
              <p>Заверши текущее занятие, чтобы начать новое.</p>
              <button className="complete-button" onClick={() => void completeSession()} disabled={completingSession || startingTopicId !== null}>
                {completingSession ? 'Завершаем…' : 'Завершить занятие'}
              </button>
            </>}
          </div>
          {startError && <p className="start-error" role="alert">{startError}</p>}
          {completionError && <p className="start-error" role="alert">{completionError}</p>}
          {completionMessage && <p className="completion-message" role="status">{completionMessage}</p>}
        </section>

        <div aria-live="polite" aria-atomic="true">
          {state.status === 'loading' && <p className="notice" role="status">Загружаем учебные темы…</p>}
          {state.status === 'error' && (
            <div className="notice error" role="alert">
              <h2>Не удалось загрузить темы</h2>
              <p>Проверь, что backend запущен, и попробуй обновить список.</p>
              <button onClick={reload}>Попробовать снова</button>
            </div>
          )}
          {state.status === 'success' && (
            <>
              <p className="summary">Всего тем: {state.topics.length} · Обязательных: {state.topics.filter(topic => !topic.niceToHave).length}</p>
              {state.topics.length === 0 ? (
                <p className="notice">Тем пока нет. Добавь учебные темы в каталог.</p>
              ) : (
                <ul className="topics">
                  {state.topics.map(topic => (
                    <li key={topic.id}>
                      <article className="topic">
                        <div className="topic-meta">
                          <span className="category">{topic.category}</span>
                          <span className={topic.niceToHave ? 'badge optional' : 'badge'}>
                            {topic.niceToHave ? 'Nice to have' : 'Обязательная'}
                          </span>
                        </div>
                        <h2>{topic.title}</h2>
                        <p className="description">{topic.description}</p>
                        <button className="start-button" disabled={cannotStart} onClick={() => void startSession(topic.id)}>
                          {startingTopicId === topic.id ? 'Запускаем…' : activeSession?.studyTopic.id === topic.id ? 'Занятие идёт' : 'Начать занятие'}
                        </button>
                      </article>
                    </li>
                  ))}
                </ul>
              )}
            </>
          )}
        </div>
      </main>
      <footer>Study Tracker · Учимся последовательно</footer>
    </div>
  )
}

export default App
