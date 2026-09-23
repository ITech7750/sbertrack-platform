import { useCallback, useEffect, useRef, useState } from 'react';
import { platformApi } from '../api/client';
import { AgentSession } from '../types';

type SessionState = {
  key: string;
  requestId: number;
  status: 'loading' | 'ready' | 'error';
  session: AgentSession | null;
  error: string | null;
};

export function useAgentSession(agentId?: string, caseId?: string, studentId?: string) {
  const key = agentId && studentId ? JSON.stringify([studentId, agentId, caseId ?? null]) : '';
  const generation = useRef(0);
  const [state, setState] = useState<SessionState>({ key: '', requestId: 0, status: 'loading', session: null, error: null });

  const reload = useCallback(async () => {
    const requestId = ++generation.current;
    setState({ key, requestId, status: 'loading', session: null, error: null });
    if (!key || !agentId) return;
    try {
      const session = await platformApi.agents.latestSession(agentId, caseId);
      if (generation.current === requestId) setState({ key, requestId, status: 'ready', session, error: null });
    } catch (error) {
      if (generation.current === requestId) setState({ key, requestId, status: 'error', session: null,
        error: error instanceof Error ? error.message : 'Не удалось загрузить историю чата' });
    }
  }, [key, agentId, caseId]);

  useEffect(() => {
    void reload();
    return () => { generation.current++; };
  }, [reload]);

  const matches = Boolean(key) && state.key === key;
  const ready = matches && state.status === 'ready';
  const requestId = state.requestId;
  return {
    ready,
    session: matches ? state.session : null,
    error: matches ? state.error : null,
    reload,
    isCurrent: () => ready && generation.current === requestId,
    setSession: (session: AgentSession) => {
      setState(current => current.key === key && current.requestId === requestId && generation.current === requestId
        ? { ...current, session } : current);
    }
  };
}
