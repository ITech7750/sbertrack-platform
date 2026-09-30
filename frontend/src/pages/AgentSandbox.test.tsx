import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AgentSandboxPage, StudentWorkspacePage } from './StudentPages';

const mocks = vi.hoisted(() => ({
  post: vi.fn(), sendMessage: vi.fn(), latestSession: vi.fn(),
  data: {
    agents: [
      { id: 'agent', name: 'Mentor', description: 'Description', specialization: 'BACKEND_ARCHITECTURE', capabilities: [] },
      { id: 'other-agent', name: 'Other mentor', description: 'Description', specialization: 'BACKEND_ARCHITECTURE', capabilities: [] }
    ],
    cases: [{ id: 'case', title: 'Case title' }],
    item: { id: 'case', title: 'Case title', fullDescription: 'Task description' }
  }
}));
vi.mock('../auth/AuthProvider', () => ({ useAuth: () => ({ session: { user: { id: 'student' } } }) }));
vi.mock('../hooks/useApi', () => ({ useApi: () => ({ data: mocks.data, loading: false, error: null, reload: vi.fn() }) }));
vi.mock('../api/client', () => ({
  get: vi.fn(), put: vi.fn(), post: mocks.post,
  platformApi: { agents: { sendMessage: mocks.sendMessage, latestSession: mocks.latestSession } }
}));

describe('agent chat delivery', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    mocks.latestSession.mockResolvedValue(null);
    mocks.post.mockResolvedValue({ id: 'session', messages: [] });
  });

  it.each([['sandbox', AgentSandboxPage], ['workspace', StudentWorkspacePage]] as const)(
    '%s waits for history before sending and continues the restored session', async (_, Page) => {
      let finishRestore!: (value: unknown) => void;
      mocks.latestSession.mockImplementationOnce(() => new Promise(resolve => { finishRestore = resolve; }));
      mocks.sendMessage.mockResolvedValueOnce({ id: 'existing', messages: [{ id: 'r', role: 'AGENT', content: 'Fresh answer' }] });
      const user = userEvent.setup();
      render(<MemoryRouter initialEntries={['/case/case']}><Routes><Route path="/case/:caseId" element={<Page />} /></Routes></MemoryRouter>);
      const input = screen.getByPlaceholderText('Помоги проверить структуру решения');
      await user.clear(input);
      await user.type(input, 'question');
      const send = screen.getByRole('button', { name: 'Отправить сообщение' });
      expect(send).toBeDisabled();
      fireEvent.keyDown(input, { key: 'Enter' });
      expect(mocks.post).not.toHaveBeenCalled();
      expect(mocks.sendMessage).not.toHaveBeenCalled();
      await act(async () => finishRestore({ id: 'existing', messages: [] }));
      await waitFor(() => expect(send).toBeEnabled());
      await user.click(send);
      expect(await screen.findByText('Fresh answer')).toBeInTheDocument();
      expect(mocks.sendMessage).toHaveBeenCalledWith('existing', expect.objectContaining({ content: 'question' }));
      expect(mocks.post).not.toHaveBeenCalled();
    }
  );

  it.each([['sandbox', AgentSandboxPage], ['workspace', StudentWorkspacePage]] as const)(
    '%s treats a restore error as an error and allows retry without creating a session', async (_, Page) => {
      mocks.latestSession.mockRejectedValueOnce(new Error('History unavailable'))
        .mockResolvedValueOnce({ id: 'existing', messages: [] });
      mocks.sendMessage.mockResolvedValueOnce({ id: 'existing', messages: [{ id: 'r', role: 'AGENT', content: 'After restore retry' }] });
      const user = userEvent.setup();
      render(<MemoryRouter initialEntries={['/case/case']}><Routes><Route path="/case/:caseId" element={<Page />} /></Routes></MemoryRouter>);
      expect(await screen.findByText('History unavailable')).toBeInTheDocument();
      const input = screen.getByPlaceholderText('Помоги проверить структуру решения');
      await user.clear(input);
      await user.type(input, 'question');
      const send = screen.getByRole('button', { name: 'Отправить сообщение' });
      expect(send).toBeDisabled();
      fireEvent.keyDown(input, { key: 'Enter' });
      expect(mocks.post).not.toHaveBeenCalled();
      await user.click(screen.getByRole('button', { name: 'Повторить загрузку' }));
      await waitFor(() => expect(send).toBeEnabled());
      await user.click(send);
      expect(await screen.findByText('After restore retry')).toBeInTheDocument();
      expect(mocks.post).not.toHaveBeenCalled();
      expect(mocks.sendMessage).toHaveBeenCalledWith('existing', expect.anything());
    }
  );

  it('ignores a late restore for a previously selected agent', async () => {
    let finishOldRestore!: (value: unknown) => void;
    mocks.latestSession.mockImplementationOnce(() => new Promise(resolve => { finishOldRestore = resolve; }))
      .mockResolvedValueOnce({ id: 'other-session', messages: [{ id: 'other', role: 'AGENT', content: 'Other history' }] });
    const user = userEvent.setup();
    render(<MemoryRouter><AgentSandboxPage /></MemoryRouter>);
    await waitFor(() => expect(mocks.latestSession).toHaveBeenCalledTimes(1));
    await user.click(screen.getByRole('tab', { name: 'Other mentor' }));
    expect(await screen.findByText('Other history')).toBeInTheDocument();
    await act(async () => finishOldRestore({ id: 'old-session', messages: [{ id: 'old', role: 'AGENT', content: 'Stale history' }] }));
    expect(screen.getByText('Other history')).toBeInTheDocument();
    expect(screen.queryByText('Stale history')).not.toBeInTheDocument();
  });

  it.each([['sandbox', AgentSandboxPage], ['workspace', StudentWorkspacePage]] as const)('%s restores draft on failure and retries in the same session', async (_, Page) => {
    const user = userEvent.setup();
    mocks.sendMessage.mockRejectedValueOnce(new Error('Наставник временно недоступен'))
      .mockResolvedValueOnce({ id: 'session', messages: [{ id: 'reply', role: 'AGENT', content: 'Recovered answer' }] });
    render(<MemoryRouter initialEntries={['/case/case']}><Routes><Route path="/case/:caseId" element={<Page />} /></Routes></MemoryRouter>);
    await waitFor(() => expect(mocks.latestSession).toHaveBeenCalled());
    const input = screen.getByPlaceholderText('Помоги проверить структуру решения');
    await user.clear(input);
    await user.type(input, 'Помоги структурировать решение кейса');
    const send = screen.getByRole('button', { name: 'Отправить сообщение' });
    await waitFor(() => expect(send).toBeEnabled());
    await user.click(send);
    expect(await screen.findByText('Наставник временно недоступен')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Помоги структурировать решение кейса')).toBeInTheDocument();
    expect(send).toBeEnabled();
    await user.click(send);
    expect(await screen.findByText('Recovered answer')).toBeInTheDocument();
    expect(mocks.post).toHaveBeenCalledTimes(1);
    expect(mocks.sendMessage).toHaveBeenCalledTimes(2);
    expect(screen.queryByText('Наставник временно недоступен')).not.toBeInTheDocument();
  });
});
