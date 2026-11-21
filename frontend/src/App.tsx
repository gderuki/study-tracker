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

function App() {
  const [state, setState] = useState<TopicsState>({ status: 'loading' })
  const [attempt, setAttempt] = useState(0)

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
