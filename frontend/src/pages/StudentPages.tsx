import {
  Background,
  BackgroundVariant,
  Controls,
  Edge,
  Handle,
  MarkerType,
  MiniMap,
  Node,
  NodeProps,
  Position,
  ReactFlow,
  ReactFlowProvider,
  useEdgesState,
  useNodesState
} from '@xyflow/react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  Checkbox,
  Chip,
  CircularProgress,
  Divider,
  Grid,
  LinearProgress,
  ListItemText,
  MenuItem,
  Paper,
  Stack,
  Tab,
  Tabs,
  TextField,
  Typography,
  alpha,
  useTheme
} from '@mui/material';
import RouteRoundedIcon from '@mui/icons-material/RouteRounded';
import SmartToyRoundedIcon from '@mui/icons-material/SmartToyRounded';
import SendRoundedIcon from '@mui/icons-material/SendRounded';
import SaveRoundedIcon from '@mui/icons-material/SaveRounded';
import FactCheckRoundedIcon from '@mui/icons-material/FactCheckRounded';
import TimelineRoundedIcon from '@mui/icons-material/TimelineRounded';
import AccountTreeRoundedIcon from '@mui/icons-material/AccountTreeRounded';
import WorkspacePremiumRoundedIcon from '@mui/icons-material/WorkspacePremiumRounded';
import CheckRoundedIcon from '@mui/icons-material/CheckRounded';
import LockRoundedIcon from '@mui/icons-material/LockRounded';
import PlayArrowRoundedIcon from '@mui/icons-material/PlayArrowRounded';
import FlagRoundedIcon from '@mui/icons-material/FlagRounded';
import ArrowForwardRoundedIcon from '@mui/icons-material/ArrowForwardRounded';
import DescriptionRoundedIcon from '@mui/icons-material/DescriptionRounded';
import SchemaRoundedIcon from '@mui/icons-material/SchemaRounded';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import FavoriteBorderRoundedIcon from '@mui/icons-material/FavoriteBorderRounded';
import ChatBubbleOutlineRoundedIcon from '@mui/icons-material/ChatBubbleOutlineRounded';
import VisibilityRoundedIcon from '@mui/icons-material/VisibilityRounded';
import SelfImprovementRoundedIcon from '@mui/icons-material/SelfImprovementRounded';
import UploadFileRoundedIcon from '@mui/icons-material/UploadFileRounded';
import AttachFileRoundedIcon from '@mui/icons-material/AttachFileRounded';
import { memo, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { get, platformApi, post, put } from '../api/client';
import { useAgentSession } from '../hooks/useAgentSession';
import { useAuth } from '../auth/AuthProvider';
import {
  BarChartBlock,
  CompetencyExplanationCards,
  CompetencyRadarChart,
  FunnelBlock,
  LineChartBlock,
  MetricGrid,
  PieChartBlock
} from '../components/AnalyticsCharts';
import { CompetencyBars } from '../components/CompetencyBars';
import { EmptyState } from '../components/EmptyState';
import { ErrorState } from '../components/ErrorState';
import { LoadingBlock } from '../components/LoadingBlock';
import { PageHeader } from '../components/PageHeader';
import { StatCard } from '../components/StatCard';
import { useApi } from '../hooks/useApi';
import { brand } from '../theme/theme';
import {
  AgentDefinition,
  AgentSession,
  Competency,
  Difficulty,
  NewsCategory,
  Portfolio,
  PracticalCase,
  ProfileTraits,
  RoadmapStatus,
  StudentRoadmap,
  Submission,
  Track,
  Trajectory,
  TrajectoryNode
} from '../types';
import {
  agentCapabilityLabels,
  agentSpecializationLabels,
  competencyDescriptions,
  competencyLabels,
  difficultyLabels,
  displayStatus,
  feedbackModeLabels,
  roadmapStatusLabels,
  submissionStatusLabels,
  trajectoryNodeTypeLabels
} from '../shared/labels';

type TrajectoryFlowNodeData = Record<string, unknown> & {
  node: TrajectoryNode;
  selected: boolean;
  onSelect: (node: TrajectoryNode) => void;
};

type TrajectoryFlowNodeModel = Node<TrajectoryFlowNodeData, 'trajectoryNode'>;

const artifactOptions = [
  'Описание решения',
  'Архитектурная схема',
  'Финансовая модель',
  'Прототип интерфейса',
  'Результаты исследования'
];

const competencyKeys = Object.keys(competencyLabels) as Competency[];

export function StudentDashboardPage() {
  const navigate = useNavigate();
  const { session } = useAuth();
  const { data, loading, error, reload } = useApi(async () => {
    const [analytics, tracks, cases, portfolio, submissions, roadmap, events] = await Promise.all([
      platformApi.analytics.studentDashboard(),
      get<Track[]>('/tracks'),
      get<PracticalCase[]>('/cases', { status: 'PUBLISHED' }),
      get<Portfolio>('/portfolio/me'),
      get<Submission[]>('/submissions', { studentId: session?.user.id }),
      platformApi.roadmaps.me(),
      platformApi.events.listUpcoming()
    ]);
    return { analytics, tracks, cases, portfolio, submissions, roadmap, events };
  }, [session?.user.id]);

  if (error) return <ErrorState message={error} onRetry={reload} />;
  if (loading || !data) return <LoadingBlock />;
  const recommendedTrack = data.tracks[0];
  const nearestCase = data.cases.find((item) => item.title === data.analytics.nearestCaseTitle) ?? data.cases[0];
  const currentStep = data.roadmap.steps.find((step) => step.nodeId === data.roadmap.currentNodeId) ?? data.roadmap.steps[0];
  const latestSubmission = data.submissions[0];

  return (
    <Box>
      <PageHeader title={`Здравствуйте, ${session?.user.fullName ?? 'участник'}`} subtitle="Ваш аналитический центр развития" />

      <Box
        sx={{
          position: 'relative',
          overflow: 'hidden',
          borderRadius: 3,
          p: { xs: 3, md: 4 },
          mb: 4,
          color: 'white',
          background: `linear-gradient(135deg, ${brand.forest} 0%, ${brand.teal} 62%, ${brand.lime} 100%)`
        }}
      >
        <Box aria-hidden sx={{ position: 'absolute', right: -120, top: -120, width: 360, height: 360, borderRadius: '50%', background: alpha('#fff', 0.07) }} />
        <Grid container spacing={4} alignItems="center" sx={{ position: 'relative' }}>
          <Grid item xs={12} md={8}>
            <Typography variant="caption" fontWeight={600} sx={{ textTransform: 'uppercase', letterSpacing: '0.05em', mb: 1, display: 'block', color: alpha('#fff', 0.72) }}>
              Текущий трек
            </Typography>
            <Typography sx={{ fontFamily: 'Manrope, sans-serif', fontWeight: 800, fontSize: { xs: 24, md: 30 }, mb: 2 }}>
              {data.analytics.trajectoryTitle}
            </Typography>
            <Typography sx={{ color: alpha('#fff', 0.85), mb: 2 }}>
              Ближайший кейс: <Typography component="span" fontWeight={700} sx={{ color: '#fff' }}>{nearestCase?.title}</Typography>
            </Typography>
            <Typography variant="body2" sx={{ color: alpha('#fff', 0.72) }}>
              Текущий этап: {currentStep?.title}
            </Typography>
          </Grid>
          <Grid item xs={12} md={4}>
            <Typography variant="body2" fontWeight={500} sx={{ mb: 1, color: alpha('#fff', 0.72) }}>Прогресс roadmap</Typography>
            <Typography sx={{ fontFamily: 'Manrope, sans-serif', fontWeight: 800, fontSize: 44, mb: 2 }}>{data.analytics.roadmapProgress}%</Typography>
            <LinearProgress
              variant="determinate"
              value={data.analytics.roadmapProgress}
              sx={{ height: 6, borderRadius: 3, mb: 3, bgcolor: alpha('#fff', 0.22), '& .MuiLinearProgress-bar': { bgcolor: '#fff', borderRadius: 3 } }}
            />
            <Button
              startIcon={<TimelineRoundedIcon />}
              onClick={() => navigate('/student/roadmap')}
              fullWidth
              sx={{ bgcolor: '#fff', color: brand.forest, '&:hover': { bgcolor: alpha('#fff', 0.9) } }}
            >
              Открыть дорожную карту
            </Button>
          </Grid>
        </Grid>
      </Box>

      <Box sx={{ mb: 4 }}>
        <MetricGrid metrics={data.analytics.metrics} />
      </Box>

      <Grid container spacing={3}>
        <Grid item xs={12} lg={7}>
          <Stack spacing={3}>
            <Card>
              <CardContent>
                <Typography variant="h5" sx={{ mb: 3 }}>Рекомендация следующего шага</Typography>
                <Typography variant="h6" sx={{ mb: 2 }}>{nearestCase?.title}</Typography>
                <Typography color="text.secondary" sx={{ mb: 3 }}>{data.analytics.recommendation}</Typography>
                {nearestCase && topCompetencies(nearestCase.competencyWeights).length > 0 && (
                  <Box sx={{ mb: 3 }}>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>Развиваемые компетенции</Typography>
                    <Stack direction="row" spacing={1}>
                      {topCompetencies(nearestCase.competencyWeights).map((competency) => (
                        <Chip key={competency} label={competencyLabels[competency]} size="small" />
                      ))}
                    </Stack>
                  </Box>
                )}
                <Stack direction="row" spacing={2}>
                  <Button variant="contained" onClick={() => nearestCase && navigate(`/student/cases/${nearestCase.id}`)}>
                    Перейти к кейсу
                  </Button>
                  <Button variant="outlined" onClick={() => navigate('/student/trajectories')}>
                    Выбрать траекторию
                  </Button>
                </Stack>
              </CardContent>
            </Card>

            <Card>
              <CardContent>
                <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
                  <Typography variant="h6">Продолжить обучение</Typography>
                  <Button size="small" onClick={() => navigate('/student/tracks')}>Все курсы →</Button>
                </Stack>
                <Stack spacing={1.5}>
                  {data.tracks.slice(0, 3).map((track) => (
                    <Paper
                      key={track.id}
                      variant="outlined"
                      sx={{ p: 2, cursor: 'pointer', transition: 'border-color 150ms ease', '&:hover': { borderColor: 'primary.main' } }}
                      onClick={() => navigate(`/student/tracks/${track.id}`)}
                    >
                      <Stack direction="row" justifyContent="space-between" alignItems="center" spacing={2}>
                        <Box sx={{ minWidth: 0 }}>
                          <Typography fontWeight={600} noWrap>{track.title}</Typography>
                          <Typography variant="body2" color="text.secondary" noWrap>{track.customerName} · {difficultyLabels[track.difficulty]}</Typography>
                        </Box>
                        <ArrowForwardRoundedIcon fontSize="small" sx={{ color: 'text.disabled', flexShrink: 0 }} />
                      </Stack>
                    </Paper>
                  ))}
                  {data.tracks.length === 0 && <Typography color="text.secondary">Пока нет доступных треков.</Typography>}
                </Stack>
              </CardContent>
            </Card>
          </Stack>
        </Grid>
        <Grid item xs={12} lg={5}>
          <Stack spacing={3}>
            <Card>
              <CardContent>
                <Typography variant="h6" sx={{ mb: 2 }}>Последняя обратная связь</Typography>
                {data.portfolio.feedbackHighlights.length ? (
                  data.portfolio.feedbackHighlights.slice(0, 3).map((text) => <Alert key={text} severity="success" sx={{ mb: 1 }}>{text}</Alert>)
                ) : (
                  <Typography color="text.secondary">Обратная связь появится после проверки решения.</Typography>
                )}
                {latestSubmission && (
                  <Box sx={{ mt: 2, pt: 2, borderTop: 1, borderColor: 'divider' }}>
                    <Typography variant="body2" color="text.secondary">Статус последнего решения</Typography>
                    <Typography variant="body1" fontWeight={600}>{submissionStatusLabels[latestSubmission.status]}</Typography>
                  </Box>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardContent>
                <Typography variant="h6" sx={{ mb: 2 }}>Ближайшие события</Typography>
                {data.events.length ? (
                  <Stack spacing={0}>
                    {data.events.map((event, index) => {
                      const date = new Date(event.startsAt);
                      return (
                        <Stack
                          key={event.id}
                          direction="row"
                          spacing={1.5}
                          sx={{
                            py: 1.5,
                            borderBottom: index < data.events.length - 1 ? 1 : 0,
                            borderColor: 'divider'
                          }}
                        >
                          <Box
                            sx={{
                              width: 44,
                              height: 44,
                              borderRadius: 1.5,
                              bgcolor: 'background.default',
                              color: brand.forest,
                              display: 'flex',
                              flexDirection: 'column',
                              alignItems: 'center',
                              justifyContent: 'center',
                              flexShrink: 0
                            }}
                          >
                            <Typography sx={{ fontFamily: 'Manrope, sans-serif', fontWeight: 800, fontSize: 15, lineHeight: 1 }}>
                              {date.toLocaleDateString('ru-RU', { day: 'numeric' })}
                            </Typography>
                            <Typography sx={{ fontSize: 9.5, fontWeight: 700, textTransform: 'uppercase' }}>
                              {date.toLocaleDateString('ru-RU', { month: 'short' })}
                            </Typography>
                          </Box>
                          <Box sx={{ minWidth: 0 }}>
                            <Typography variant="body2" fontWeight={600}>{event.title}</Typography>
                            <Typography variant="caption" color="text.disabled">{event.location}</Typography>
                          </Box>
                        </Stack>
                      );
                    })}
                  </Stack>
                ) : (
                  <Typography color="text.secondary">Пока нет запланированных событий.</Typography>
                )}
              </CardContent>
            </Card>
          </Stack>
        </Grid>
      </Grid>
    </Box>
  );
}

export function StudentAnalyticsPage() {
  const { session } = useAuth();
  const { data, loading, error, reload } = useApi(() => platformApi.analytics.studentDashboard(), [session?.user.id]);

  if (error) return <ErrorState message={error} onRetry={reload} />;
  if (loading || !data) return <LoadingBlock />;

  return (
    <Box>
      <PageHeader title="Аналитика развития" subtitle="Компетенции, качество решений и прогресс по трекам" />
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} lg={4}>
          <PieChartBlock title="Выполненные кейсы по направлениям" data={data.completedCasesByDirection} />
        </Grid>
        <Grid item xs={12} lg={4}>
          <BarChartBlock title="Рост компетенций по последним кейсам" data={data.competencyGrowth} />
        </Grid>
        <Grid item xs={12} lg={4}>
          <CompetencyRadarChart data={data.competencyRadar} />
        </Grid>
        <Grid item xs={12} lg={6}>
          <LineChartBlock title="Динамика качества решений" data={data.qualityDynamics} />
        </Grid>
        <Grid item xs={12} lg={6}>
          <FunnelBlock title="Мини-воронка прогресса" data={data.funnel} />
        </Grid>
      </Grid>
    </Box>
  );
}

export function TrackCatalogPage() {
  const navigate = useNavigate();
  const [difficulty, setDifficulty] = useState<Difficulty | ''>('');
  const [audience, setAudience] = useState('');
  const [competency, setCompetency] = useState<Competency | ''>('');
  const [customer, setCustomer] = useState('');
  const { data, loading, error, reload } = useApi(() => get<Track[]>('/tracks', {
    difficulty: difficulty || undefined,
    targetAudience: audience || undefined,
    competency: competency || undefined,
    customerName: customer || undefined
  }), [difficulty, audience, competency, customer]);

  return (
    <Box>
      <PageHeader title="Каталог треков" subtitle="Фильтры по сложности, аудитории, компетенциям и заказчику" />
      <Card sx={{ mb: 4 }}>
        <CardContent>
          <Grid container spacing={2}>
            <Grid item xs={12} md={3}>
              <TextField select label="Сложность" value={difficulty} onChange={(event) => setDifficulty(event.target.value as Difficulty | '')} fullWidth size="small">
                <MenuItem value="">Все</MenuItem>
                {Object.entries(difficultyLabels).map(([value, label]) => <MenuItem value={value} key={value}>{label}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12} md={3}><TextField label="Аудитория" value={audience} onChange={(event) => setAudience(event.target.value)} fullWidth size="small" /></Grid>
            <Grid item xs={12} md={3}>
              <TextField select label="Компетенция" value={competency} onChange={(event) => setCompetency(event.target.value as Competency | '')} fullWidth size="small">
                <MenuItem value="">Все</MenuItem>
                {competencyKeys.map((item) => <MenuItem value={item} key={item}>{competencyLabels[item]}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12} md={3}><TextField label="Заказчик" value={customer} onChange={(event) => setCustomer(event.target.value)} fullWidth size="small" /></Grid>
          </Grid>
        </CardContent>
      </Card>
      {error ? <ErrorState message={error} onRetry={reload} /> : loading || !data ? <LoadingBlock /> : (
        <Grid container spacing={3}>
          {data.map((track) => (
            <Grid item xs={12} md={6} lg={4} key={track.id}>
              <Card sx={{
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                cursor: 'pointer',
                transition: 'all 150ms ease',
                '&:hover': {
                  transform: 'translateY(-2px)',
                  borderColor: 'primary.main'
                }
              }}
              onClick={() => navigate(`/student/tracks/${track.id}`)}
              >
                <CardContent sx={{ flexGrow: 1 }}>
                  <Typography variant="caption" color="text.secondary" fontWeight={600} sx={{ textTransform: 'uppercase', letterSpacing: '0.05em', mb: 1, display: 'block' }}>
                    {track.customerName}
                  </Typography>
                  <Typography variant="h6" sx={{ mb: 2 }}>{track.title}</Typography>
                  <Typography color="text.secondary" variant="body2" sx={{ mb: 3 }}>{track.description}</Typography>
                  <Stack direction="row" spacing={1} sx={{ mt: 'auto' }}>
                    <Typography variant="body2" fontWeight={600}>{difficultyLabels[track.difficulty]}</Typography>
                    <Typography variant="body2" color="text.secondary">·</Typography>
                    <Typography variant="body2" color="text.secondary">{track.caseIds.length} кейсов</Typography>
                  </Stack>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
    </Box>
  );
}

export function TrackDetailsPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { data, loading, error, reload } = useApi(async () => {
    const [track, cases] = await Promise.all([
      get<Track>(`/tracks/${id}`),
      get<PracticalCase[]>('/cases', { trackId: id })
    ]);
    return { track, cases };
  }, [id]);

  if (error) return <ErrorState message={error} onRetry={reload} />;
  if (loading || !data) return <LoadingBlock />;
  return (
    <Box>
      <Button
        startIcon={<ArrowBackRoundedIcon />}
        onClick={() => navigate('/student/tracks')}
        sx={{ mb: 2 }}
      >
        К списку треков
      </Button>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h3" sx={{ mb: 1 }}>{data.track.title}</Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>{data.track.description}</Typography>
        <Stack direction="row" spacing={2} alignItems="center">
          <Typography variant="body2" color="text.secondary">{data.track.customerName}</Typography>
          <Typography variant="body2" color="text.secondary">·</Typography>
          <Typography variant="body2" fontWeight={600}>{difficultyLabels[data.track.difficulty]}</Typography>
          <Typography variant="body2" color="text.secondary">·</Typography>
          <Typography variant="body2" color="text.secondary">{data.track.targetAudience}</Typography>
        </Stack>
      </Box>
      <Grid container spacing={3}>
        {data.cases.map((item) => (
          <Grid item xs={12} md={6} lg={4} key={item.id}>
            <CaseCard item={item} onOpen={() => navigate(`/student/cases/${item.id}`)} />
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}

export function CaseDetailsPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { data: item, loading, error, reload } = useApi(() => get<PracticalCase>(`/cases/${id}`), [id]);
  if (error) return <ErrorState message={error} onRetry={reload} />;
  if (loading || !item) return <LoadingBlock />;
  return (
    <Box>
      <Button
        startIcon={<ArrowBackRoundedIcon />}
        onClick={() => navigate(-1)}
        sx={{ mb: 2 }}
      >
        Назад
      </Button>
      <PageHeader
        title={item.title}
        subtitle={item.shortDescription}
        actions={
          <Button variant="contained" size="large" onClick={() => navigate(`/student/workspace/${item.id}`)}>
            Начать выполнение
          </Button>
        }
      />
      <Grid container spacing={3}>
        <Grid item xs={12} lg={8}>
          <Card>
            <CardContent>
              <Typography variant="h5" sx={{ mb: 2 }}>Описание</Typography>
              <Typography sx={{ whiteSpace: 'pre-line', mb: 4 }}>{item.fullDescription}</Typography>
              <Typography variant="h6" sx={{ mb: 2 }}>Ожидаемый результат</Typography>
              <Typography>{item.expectedResult}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} lg={4}>
          <Card>
            <CardContent>
              <Stack spacing={2.5}>
                <Box>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>Статус</Typography>
                  <Typography variant="body1" fontWeight={600}>{displayStatus(item.status)}</Typography>
                </Box>
                <Box>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>Дедлайн</Typography>
                  <Typography variant="body1" fontWeight={600}>{new Date(item.deadline).toLocaleDateString('ru-RU')}</Typography>
                </Box>
                <Box>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>Лимит участников</Typography>
                  <Typography variant="body1" fontWeight={600}>{item.participantLimit}</Typography>
                </Box>
                <Box>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>Формат обратной связи</Typography>
                  <Typography variant="body1" fontWeight={600}>{feedbackModeLabels[item.feedbackMode]}</Typography>
                </Box>
                {item.tags.length > 0 && (
                  <Box>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>Теги</Typography>
                    <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap' }}>
                      {item.tags.map((tag) => (
                        <Chip size="small" key={tag} label={tag} />
                      ))}
                    </Stack>
                  </Box>
                )}
                <Box>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>Компетенции</Typography>
                  <CompetencyBars values={item.competencyWeights} compact />
                </Box>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}

export function StudentWorkspacePage() {
  const { caseId } = useParams();
  const { session } = useAuth();
  const [solution, setSolution] = useState('');
  const [artifactUrl, setArtifactUrl] = useState('');
  const [artifactFileName, setArtifactFileName] = useState('');
  const [uploadingArtifact, setUploadingArtifact] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const [selectedArtifacts, setSelectedArtifacts] = useState<string[]>(['Описание решения']);
  const [submission, setSubmission] = useState<Submission | null>(null);
  const [message, setMessage] = useState('');
  const [sendingMessage, setSendingMessage] = useState(false);
  const [pendingMessage, setPendingMessage] = useState('');
  const [chatError, setChatError] = useState<string | null>(null);

  async function handleArtifactUpload(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file) return;
    setUploadingArtifact(true);
    setUploadError(null);
    try {
      const uploaded = await platformApi.files.upload(file);
      setArtifactUrl(uploaded.url);
      setArtifactFileName(uploaded.fileName);
    } catch (uploadException) {
      setUploadError(uploadException instanceof Error ? uploadException.message : 'Не удалось загрузить файл');
    } finally {
      setUploadingArtifact(false);
    }
  }
  const { data, loading, error, reload } = useApi(async () => {
    const [item, agents] = await Promise.all([
      get<PracticalCase>(`/cases/${caseId}`),
      get<AgentDefinition[]>('/agents')
    ]);
    return { item, agents };
  }, [caseId]);
  const mentor = data?.agents[0];

  const chat = useAgentSession(mentor?.id, caseId, session?.user.id);
  useEffect(() => {
    setChatError(null);
    setSendingMessage(false);
    setPendingMessage('');
  }, [mentor?.id, caseId]);

  async function ensureSubmission(): Promise<Submission> {
    if (submission) return submission;
    const created = await post<Submission>('/submissions', {
      caseId,
      studentId: session?.user.id,
      teamName: 'Команда участника',
      title: data?.item.title ?? 'Решение кейса',
      description: solution,
      artifactUrl: artifactUrl || null
    });
    setSubmission(created);
    return created;
  }

  async function saveDraft() {
    const current = await ensureSubmission();
    const saved = await put<Submission>(`/submissions/${current.id}`, {
      teamName: current.teamName,
      title: current.title,
      description: solution,
      artifactUrl: artifactUrl || null
    });
    setSubmission(saved);
  }

  async function submitSolution() {
    const current = await ensureSubmission();
    const saved = await put<Submission>(`/submissions/${current.id}`, {
      teamName: current.teamName,
      title: current.title,
      description: solution,
      artifactUrl: artifactUrl || null
    });
    const submitted = await post<Submission>(`/submissions/${saved.id}/submit`);
    setSubmission(submitted);
  }

  async function sendAgentMessage() {
    if (!message.trim() || !mentor || !data || sendingMessage || !chat.ready) return;
    const content = message;
    setMessage('');
    setPendingMessage(content);
    setSendingMessage(true);
    setChatError(null);
    try {
      const currentSession = chat.session ?? await post<AgentSession>('/agents/sessions', {
        studentId: session?.user.id,
        caseId,
        agentId: mentor.id
      });
      if (!chat.isCurrent()) return;
      chat.setSession(currentSession);
      const artifacts = artifactUrl ? [...selectedArtifacts, artifactUrl] : selectedArtifacts;
      const nextSession = await platformApi.agents.sendMessage(currentSession.id, {
        content,
        caseTitle: data.item.title,
        artifacts
      });
      if (!chat.isCurrent()) return;
      chat.setSession(nextSession);
    } catch (error) {
      if (!chat.isCurrent()) return;
      setMessage(content);
      setChatError(error instanceof Error ? error.message : 'Не удалось отправить сообщение');
    } finally {
      if (chat.isCurrent()) {
        setPendingMessage('');
        setSendingMessage(false);
      }
    }
  }

  if (error) return <ErrorState message={error} onRetry={reload} />;
  if (loading || !data || !mentor) return <LoadingBlock />;
  return (
    <Box>
      <PageHeader title="Рабочая зона кейса" subtitle={data.item.title} />
      <Alert severity="info" sx={{ mb: 2 }}>
        ИИ-наставник помогает мыслить, проверять гипотезы и структурировать работу. Итоговое решение оформляет сам участник.
      </Alert>
      <Grid container spacing={2}>
        <Grid item xs={12} lg={7}>
          <Card sx={{ mb: 2 }}>
            <CardContent>
              <Typography variant="h6">Описание кейса</Typography>
              <Typography color="text.secondary" sx={{ mt: 1 }}>{data.item.fullDescription}</Typography>
            </CardContent>
          </Card>
          <Card>
            <CardContent>
              <Stack spacing={2}>
                <TextField label="Заметки и решение" value={solution} onChange={(event) => setSolution(event.target.value)} multiline minRows={9} fullWidth />
                <Stack spacing={1}>
                  <Stack direction="row" spacing={1.5} alignItems="center">
                    <Button
                      component="label"
                      variant="outlined"
                      startIcon={uploadingArtifact ? <CircularProgress size={16} /> : <UploadFileRoundedIcon />}
                      disabled={uploadingArtifact}
                    >
                      {uploadingArtifact ? 'Загрузка…' : 'Прикрепить файл'}
                      <input type="file" hidden onChange={handleArtifactUpload} />
                    </Button>
                    {artifactFileName && (
                      <Chip icon={<AttachFileRoundedIcon />} label={artifactFileName} size="small" onDelete={() => { setArtifactUrl(''); setArtifactFileName(''); }} />
                    )}
                  </Stack>
                  {uploadError && <Alert severity="error" onClose={() => setUploadError(null)}>{uploadError}</Alert>}
                </Stack>
                <TextField
                  select
                  label="Наработки"
                  value={selectedArtifacts}
                  onChange={(event) => setSelectedArtifacts(typeof event.target.value === 'string' ? event.target.value.split(',') : event.target.value as string[])}
                  SelectProps={{ multiple: true, renderValue: (selected) => (selected as string[]).join(', ') }}
                  fullWidth
                >
                  {artifactOptions.map((artifact) => (
                    <MenuItem key={artifact} value={artifact}>
                      <Checkbox checked={selectedArtifacts.includes(artifact)} />
                      <ListItemText primary={artifact} />
                    </MenuItem>
                  ))}
                </TextField>
                {submission && <Alert severity="success">Статус решения: {submissionStatusLabels[submission.status]}</Alert>}
                <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
                  <Button variant="outlined" startIcon={<SaveRoundedIcon />} onClick={saveDraft}>Сохранить черновик</Button>
                  <Button variant="contained" startIcon={<SendRoundedIcon />} onClick={submitSolution}>Отправить на проверку</Button>
                </Stack>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} lg={5}>
          <MentorChatPanel
            title={mentor.name}
            caseTitle={data.item.title}
            artifacts={artifactUrl ? [...selectedArtifacts, artifactUrl] : selectedArtifacts}
            session={chat.session}
            message={message}
            onMessage={setMessage}
            onSend={sendAgentMessage}
            sending={sendingMessage}
            pendingMessage={pendingMessage}
            error={chatError}
            restoring={!chat.ready && !chat.error}
            restoreError={chat.error}
            onRetryRestore={() => { void chat.reload(); }}
          />
        </Grid>
      </Grid>
    </Box>
  );
}

export function StudentTrajectoryTreePage() {
  const theme = useTheme();
  const [trajectoryId, setTrajectoryId] = useState<string>('');
  const [selectedNode, setSelectedNode] = useState<TrajectoryNode | null>(null);
  const { data, loading, error, reload } = useApi(async () => {
    const trajectories = await platformApi.trajectories.list();
    const id = trajectoryId || trajectories[0]?.id;
    const nodes = id ? await platformApi.trajectories.nodes(id) : [];
    return { trajectories, nodes, activeId: id };
  }, [trajectoryId]);
  const activeTrajectory = data?.trajectories.find((item) => item.id === data.activeId);

  async function selectTrajectory(item: Trajectory) {
    await platformApi.trajectories.select(item.id);
    await reload();
  }

  if (error) return <ErrorState message={error} onRetry={reload} />;
  if (loading || !data || !activeTrajectory) return <LoadingBlock />;

  return (
    <Box>
      <PageHeader
        title="Дерево траекторий"
        subtitle="Выбери, к какому маяку профессии ты хочешь двигаться"
        actions={
          <Button variant="contained" startIcon={<TimelineRoundedIcon />} onClick={() => selectTrajectory(activeTrajectory)}>
            Выбрать траекторию
          </Button>
        }
      />
      <Grid container spacing={3}>
        <Grid item xs={12} lg={8}>
          <Card sx={{ overflow: 'hidden' }}>
            <CardContent>
              <Tabs value={data.activeId} onChange={(_, value) => { setTrajectoryId(value); setSelectedNode(null); }} variant="scrollable" sx={{ mb: 3 }}>
                {data.trajectories.map((trajectory) => <Tab key={trajectory.id} value={trajectory.id} label={trajectory.title} />)}
              </Tabs>
              <TrajectoryFlowCanvas
                nodes={data.nodes}
                selectedNodeId={selectedNode?.id}
                onSelectNode={setSelectedNode}
                primaryColor={theme.palette.primary.main}
              />
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} lg={4}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 1 }}>{activeTrajectory.title}</Typography>
              <Typography color="text.secondary" sx={{ mb: 3 }}>{activeTrajectory.description}</Typography>
              {selectedNode ? (
                <Stack spacing={2.5}>
                  <Box>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>Тип</Typography>
                    <Typography variant="body1" fontWeight={600}>{trajectoryNodeTypeLabels[selectedNode.type]}</Typography>
                  </Box>
                  <Box>
                    <Typography variant="h5" sx={{ mb: 1 }}>{selectedNode.title}</Typography>
                    <Typography color="text.secondary">{selectedNode.description}</Typography>
                  </Box>
                  <Box>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>Развивает компетенции</Typography>
                    <CompetencyBars values={selectedNode.requiredCompetencies} compact />
                  </Box>
                  <Button variant="contained" onClick={() => selectTrajectory(activeTrajectory)}>Выбрать траекторию</Button>
                </Stack>
              ) : (
                <EmptyState title="Выберите узел дерева" description="Кликните по этапу, чтобы увидеть описание и вклад в компетенции." />
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}

function TrajectoryFlowCanvas({
  nodes: trajectoryNodes,
  selectedNodeId,
  onSelectNode,
  primaryColor
}: {
  nodes: TrajectoryNode[];
  selectedNodeId?: string;
  onSelectNode: (node: TrajectoryNode) => void;
  primaryColor: string;
}) {
  const flowNodes = useMemo<TrajectoryFlowNodeModel[]>(() => trajectoryNodes.map((node, index) => ({
    id: node.id,
    type: 'trajectoryNode',
    position: {
      x: node.positionX * 1.05,
      y: node.positionY + (index % 2 === 0 ? 22 : 76)
    },
    data: {
      node,
      selected: selectedNodeId === node.id,
      onSelect: onSelectNode
    }
  })), [trajectoryNodes, selectedNodeId, onSelectNode]);

  const flowEdges = useMemo<Edge[]>(() => {
    const nodeIds = new Set(trajectoryNodes.map((node) => node.id));
    return trajectoryNodes.flatMap((node) => node.nextNodeIds
      .filter((nextId) => nodeIds.has(nextId))
      .map((nextId) => ({
        id: `${node.id}-${nextId}`,
        source: node.id,
        target: nextId,
        type: 'smoothstep',
        animated: node.status === 'IN_PROGRESS' || node.status === 'AVAILABLE',
        markerEnd: { type: MarkerType.ArrowClosed, color: primaryColor },
        style: {
          stroke: node.status === 'LOCKED' ? '#94a3b8' : primaryColor,
          strokeWidth: node.status === 'LOCKED' ? 1.8 : 3,
          opacity: node.status === 'LOCKED' ? 0.48 : 0.86
        }
      })));
  }, [trajectoryNodes, primaryColor]);

  const [nodes, setNodes, onNodesChange] = useNodesState<TrajectoryFlowNodeModel>(flowNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState<Edge>(flowEdges);

  useEffect(() => {
    setNodes(flowNodes);
  }, [flowNodes, setNodes]);

  useEffect(() => {
    setEdges(flowEdges);
  }, [flowEdges, setEdges]);

  return (
    <Box
      sx={{
        height: { xs: 560, lg: 650 },
        borderRadius: 2,
        overflow: 'hidden',
        border: '1px solid rgba(11, 122, 100, 0.18)',
        background:
          'radial-gradient(circle at 18% 18%, rgba(11,122,100,0.16), transparent 30%), radial-gradient(circle at 88% 28%, rgba(37,99,235,0.12), transparent 32%), linear-gradient(135deg, #f6fbf8 0%, #edf7f5 52%, #f7fbff 100%)',
        '& .react-flow__controls': {
          borderRadius: 1,
          overflow: 'hidden',
          boxShadow: '0 14px 36px rgba(23,33,43,0.12)'
        },
        '& .react-flow__minimap': {
          borderRadius: 2,
          overflow: 'hidden',
          border: '1px solid rgba(11,122,100,0.16)',
          boxShadow: '0 14px 36px rgba(23,33,43,0.12)'
        },
        '& .react-flow__attribution': {
          bgcolor: 'rgba(255,255,255,0.72)'
        }
      }}
    >
      <ReactFlowProvider>
        <ReactFlow
          nodes={nodes}
          edges={edges}
          nodeTypes={trajectoryNodeTypes}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          onNodeClick={(_, node) => onSelectNode((node.data as TrajectoryFlowNodeData).node)}
          fitView
          fitViewOptions={{ padding: 0.22, minZoom: 0.55, maxZoom: 1.05 }}
          minZoom={0.35}
          maxZoom={1.35}
          panOnScroll
          selectionOnDrag
          proOptions={{ hideAttribution: true }}
        >
          <Background variant={BackgroundVariant.Dots} gap={26} size={1.15} color="rgba(11,122,100,0.22)" />
          <Controls position="bottom-left" showInteractive={false} />
          <MiniMap
            position="bottom-right"
            pannable
            zoomable
            nodeColor={(node) => nodeColor((node.data as TrajectoryFlowNodeData).node.status)}
            nodeStrokeWidth={3}
            maskColor="rgba(7, 32, 28, 0.08)"
          />
        </ReactFlow>
      </ReactFlowProvider>
    </Box>
  );
}

const TrajectoryFlowNodeCard = memo(function TrajectoryFlowNodeCard({
  data,
  selected
}: NodeProps<TrajectoryFlowNodeModel>) {
  const theme = useTheme();
  const node = data.node;
  const statusColor = nodeColor(node.status);
  const selectedState = selected || data.selected;
  const topRequiredCompetencies = topCompetencies(node.requiredCompetencies);
  const isLocked = node.status === 'LOCKED';

  return (
    <Paper
      elevation={0}
      onClick={() => data.onSelect(node)}
      sx={{
        width: 270,
        minHeight: 140,
        p: 2.5,
        cursor: 'pointer',
        borderRadius: 2,
        position: 'relative',
        border: selectedState ? `2px solid ${theme.palette.primary.main}` : `1px solid ${theme.palette.divider}`,
        bgcolor: 'background.paper',
        boxShadow: 'none',
        transition: 'all 150ms ease',
        opacity: isLocked ? 0.6 : 1,
        '&:hover': {
          transform: 'translateY(-2px)',
          borderColor: theme.palette.primary.main
        }
      }}
    >
      <Handle
        type="target"
        position={Position.Left}
        style={{ width: 8, height: 8, borderColor: theme.palette.divider, background: statusColor }}
      />
      <Handle
        type="source"
        position={Position.Right}
        style={{ width: 8, height: 8, borderColor: theme.palette.divider, background: statusColor }}
      />

      <Stack spacing={1.5}>
        <Stack direction="row" alignItems="center" justifyContent="space-between" spacing={1}>
          <Typography variant="caption" color="text.secondary" fontWeight={600} sx={{ textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            {trajectoryNodeTypeLabels[node.type]}
          </Typography>
          <Box
            sx={{
              width: 32,
              height: 32,
              borderRadius: 1.5,
              display: 'grid',
              placeItems: 'center',
              color: statusColor,
              bgcolor: alpha(statusColor, 0.08),
              border: `1px solid ${alpha(statusColor, 0.12)}`
            }}
          >
            {node.type === 'FINAL_PROJECT' ? <WorkspacePremiumRoundedIcon sx={{ fontSize: 18 }} /> : <AccountTreeRoundedIcon sx={{ fontSize: 18 }} />}
          </Box>
        </Stack>

        <Box>
          <Typography variant="body1" fontWeight={600} lineHeight={1.3} sx={{ mb: 0.5 }}>
            {node.title}
          </Typography>
          <Typography
            variant="body2"
            color="text.secondary"
            sx={{
              display: '-webkit-box',
              WebkitLineClamp: 2,
              WebkitBoxOrient: 'vertical',
              overflow: 'hidden'
            }}
          >
            {node.description}
          </Typography>
        </Box>

        <Box>
          <Typography variant="body2" fontWeight={600} color={statusColor}>
            {roadmapStatusLabels[node.status]}
          </Typography>
          {topRequiredCompetencies.length > 0 && (
            <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, display: 'block' }}>
              {topRequiredCompetencies.map(c => competencyLabels[c]).join(', ')}
            </Typography>
          )}
        </Box>
      </Stack>
    </Paper>
  );
});

const trajectoryNodeTypes = {
  trajectoryNode: TrajectoryFlowNodeCard
};

export function StudentRoadmapPage() {
  const navigate = useNavigate();
  const { data, loading, error, reload } = useApi(async () => {
    const [roadmap, cases] = await Promise.all([
      platformApi.roadmaps.me(),
      get<PracticalCase[]>('/cases')
    ]);
    return { roadmap, cases };
  }, []);

  async function startStep(roadmap: StudentRoadmap, stepId: string) {
    await platformApi.roadmaps.startStep(roadmap.id, stepId);
    await reload();
  }

  async function completeStep(roadmap: StudentRoadmap, stepId: string) {
    await platformApi.roadmaps.completeStep(roadmap.id, stepId);
    await reload();
  }

  if (error) return <ErrorState message={error} onRetry={reload} />;
  if (loading || !data) return <LoadingBlock />;
  const casesById = new Map(data.cases.map((item) => [item.id, item]));
  const steps = data.roadmap.steps;
  const completed = steps.filter((s) => s.status === 'COMPLETED').length;

  return (
    <Box>
      <PageHeader title="Мой roadmap" subtitle={data.roadmap.title} />

      <Box
        sx={{
          position: 'relative',
          overflow: 'hidden',
          borderRadius: 3,
          p: { xs: 3, md: 4 },
          mb: 4,
          color: 'white',
          background: `linear-gradient(135deg, ${brand.forest} 0%, ${brand.teal} 62%, ${brand.lime} 100%)`
        }}
      >
        <Box aria-hidden sx={{ position: 'absolute', right: -120, top: -120, width: 360, height: 360, borderRadius: '50%', background: alpha('#fff', 0.07) }} />
        <Grid container spacing={4} alignItems="flex-end" sx={{ position: 'relative' }}>
          <Grid item xs={12} md={8}>
            <Typography variant="caption" fontWeight={600} sx={{ textTransform: 'uppercase', letterSpacing: '0.05em', mb: 1, display: 'block', color: alpha('#fff', 0.72) }}>
              Прогресс дорожной карты
            </Typography>
            <Stack direction="row" alignItems="baseline" spacing={2} sx={{ mb: 2 }}>
              <Typography sx={{ fontFamily: 'Manrope, sans-serif', fontWeight: 800, fontSize: 44 }}>{data.roadmap.progressPercent}%</Typography>
              <Typography sx={{ color: alpha('#fff', 0.72) }}>{completed} из {steps.length} этапов</Typography>
            </Stack>
            <LinearProgress
              variant="determinate"
              value={data.roadmap.progressPercent}
              sx={{
                height: 6,
                borderRadius: 3,
                bgcolor: alpha('#fff', 0.22),
                '& .MuiLinearProgress-bar': { bgcolor: '#fff', borderRadius: 3 }
              }}
            />
          </Grid>
          <Grid item xs={12} md={4}>
            <Box sx={{ p: 2.5, borderRadius: 2, bgcolor: alpha('#fff', 0.12), border: `1px solid ${alpha('#fff', 0.18)}` }}>
              <Stack direction="row" spacing={1.5} alignItems="center">
                <FlagRoundedIcon sx={{ color: '#fff' }} />
                <Box>
                  <Typography variant="caption" sx={{ color: alpha('#fff', 0.72) }}>Ожидаемое завершение</Typography>
                  <Typography fontWeight={700} sx={{ color: '#fff' }}>{new Date(data.roadmap.expectedFinishDate).toLocaleDateString('ru-RU', { day: 'numeric', month: 'long', year: 'numeric' })}</Typography>
                </Box>
              </Stack>
            </Box>
          </Grid>
        </Grid>
      </Box>

      <Stack spacing={0}>
        {steps.map((step, index) => {
          const linkedCase = step.caseId ? casesById.get(step.caseId) : undefined;
          const isFinal = index === steps.length - 1;
          return (
            <RoadmapTimelineRow
              key={step.id}
              step={step}
              index={index}
              isFinal={isFinal}
              title={isFinal ? 'Маяк профессии' : step.title}
              linkedCase={linkedCase}
              onOpenCase={() => linkedCase && navigate(`/student/cases/${linkedCase.id}`)}
              onStart={() => startStep(data.roadmap, step.id)}
              onComplete={() => completeStep(data.roadmap, step.id)}
            />
          );
        })}
      </Stack>
    </Box>
  );
}

export function AgentSandboxPage() {
  const { session } = useAuth();
  const [selected, setSelected] = useState(0);
  const [text, setText] = useState('Помоги структурировать решение кейса');
  const [sendingMessage, setSendingMessage] = useState(false);
  const [pendingMessage, setPendingMessage] = useState('');
  const [chatError, setChatError] = useState<string | null>(null);
  const [caseId, setCaseId] = useState('');
  const [artifacts, setArtifacts] = useState<string[]>(['Описание решения']);
  const [artifactLink, setArtifactLink] = useState('');
  const { data, loading, error, reload } = useApi(async () => {
    const [agents, cases] = await Promise.all([
      get<AgentDefinition[]>('/agents'),
      get<PracticalCase[]>('/cases', { status: 'PUBLISHED' })
    ]);
    return { agents, cases };
  }, []);
  const agent = data?.agents[selected];
  const selectedCase = data?.cases.find((item) => item.id === caseId) ?? data?.cases[0];

  const chat = useAgentSession(agent?.id, selectedCase?.id, session?.user.id);
  useEffect(() => {
    setChatError(null);
    setSendingMessage(false);
    setPendingMessage('');
  }, [agent?.id, selectedCase?.id]);

  async function send() {
    if (!agent || !text.trim() || !selectedCase || sendingMessage || !chat.ready) return;
    const content = text;
    setText('');
    setPendingMessage(content);
    setSendingMessage(true);
    setChatError(null);
    try {
      const currentSession = chat.session ?? await post<AgentSession>('/agents/sessions', {
        studentId: session?.user.id,
        caseId: selectedCase.id,
        agentId: agent.id
      });
      if (!chat.isCurrent()) return;
      chat.setSession(currentSession);
      const next = await platformApi.agents.sendMessage(currentSession.id, {
        content,
        caseTitle: selectedCase.title,
        artifacts: artifactLink ? [...artifacts, artifactLink] : artifacts
      });
      if (!chat.isCurrent()) return;
      chat.setSession(next);
    } catch (error) {
      if (!chat.isCurrent()) return;
      setText(content);
      setChatError(error instanceof Error ? error.message : 'Не удалось отправить сообщение');
    } finally {
      if (chat.isCurrent()) {
        setPendingMessage('');
        setSendingMessage(false);
      }
    }
  }

  if (error) return <ErrorState message={error} onRetry={reload} />;
  if (loading || !data || !agent || !selectedCase) return <LoadingBlock />;
  return (
    <Box>
      <PageHeader title="Песочница ИИ-наставников" subtitle="Наставники помогают думать, проверять гипотезы и структурировать работу — но не решают за вас" />
      <Tabs value={selected} onChange={(_, value) => { setSelected(value); }} variant="scrollable" sx={{ mb: 3 }}>
        {data.agents.map((item) => <Tab key={item.id} label={item.name} disabled={sendingMessage} />)}
      </Tabs>
      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 1 }}>{agent.name}</Typography>
              <Typography variant="caption" color="text.secondary" fontWeight={600} sx={{ textTransform: 'uppercase', letterSpacing: '0.05em', mb: 2, display: 'block' }}>
                {agentSpecializationLabels[agent.specialization]}
              </Typography>
              <Typography color="text.secondary" variant="body2" sx={{ mb: 3 }}>{agent.description}</Typography>
              {agent.capabilities.length > 0 && (
                <Box sx={{ mb: 3 }}>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>Возможности</Typography>
                  <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap' }}>
                    {agent.capabilities.map((capability) => (
                      <Chip size="small" key={capability} label={agentCapabilityLabels[capability] ?? capability} />
                    ))}
                  </Stack>
                </Box>
              )}
              <TextField select label="Кейс" disabled={sendingMessage} value={selectedCase.id} onChange={(event) => { setCaseId(event.target.value); }} fullWidth sx={{ mb: 2 }} size="small">
                {data.cases.map((item) => <MenuItem key={item.id} value={item.id}>{item.title}</MenuItem>)}
              </TextField>
              <TextField
                select
                label="Наработки"
                value={artifacts}
                onChange={(event) => setArtifacts(typeof event.target.value === 'string' ? event.target.value.split(',') : event.target.value as string[])}
                SelectProps={{ multiple: true, renderValue: (selectedItems) => (selectedItems as string[]).join(', ') }}
                fullWidth
                sx={{ mb: 2 }}
                size="small"
              >
                {artifactOptions.map((artifact) => (
                  <MenuItem key={artifact} value={artifact}>
                    <Checkbox checked={artifacts.includes(artifact)} />
                    <ListItemText primary={artifact} />
                  </MenuItem>
                ))}
              </TextField>
              <TextField label="Ссылка на артефакт или название файла" value={artifactLink} onChange={(event) => setArtifactLink(event.target.value)} fullWidth size="small" />
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={8}>
          <MentorChatPanel
            title={agent.name}
            caseTitle={selectedCase.title}
            artifacts={artifactLink ? [...artifacts, artifactLink] : artifacts}
            session={chat.session}
            message={text}
            onMessage={setText}
            onSend={send}
            sending={sendingMessage}
            pendingMessage={pendingMessage}
            error={chatError}
            restoring={!chat.ready && !chat.error}
            restoreError={chat.error}
            onRetryRestore={() => { void chat.reload(); }}
          />
        </Grid>
      </Grid>
    </Box>
  );
}

export function PortfolioPage() {
  const { session } = useAuth();
  const { data, loading, error, reload } = useApi(async () => {
    const [portfolio, submissions, cases] = await Promise.all([
      get<Portfolio>('/portfolio/me'),
      get<Submission[]>('/submissions'),
      get<PracticalCase[]>('/cases')
    ]);
    return { portfolio, submissions, cases };
  }, []);
  if (error) return <ErrorState message={error} onRetry={reload} />;
  if (loading || !data) return <LoadingBlock />;
  const radar = competencyKeys.map((competency) => ({ competency, value: data.portfolio.competencyProfile[competency] ?? 0 }));
  const topCompetency = [...radar].sort((a, b) => b.value - a.value)[0];
  const artifacts = data.portfolio.artifacts.map((artifact, index) => ({
    artifact,
    caseTitle: data.portfolio.completedCases[index % Math.max(data.portfolio.completedCases.length, 1)] ?? 'Кейс в работе',
    type: index % 2 ? 'Документ' : 'Схема',
    date: `0${index + 2}.07.2026`,
    status: index % 2 ? 'Готово к демонстрации' : 'Портфолио развивается'
  }));

  return (
    <Box>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h3" sx={{ mb: 1 }}>Моё портфолио</Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>{data.portfolio.summary}</Typography>
        <Stack direction="row" spacing={2} alignItems="center">
          <Typography variant="body2" color="text.secondary">{data.portfolio.completedCases.length} завершённых кейсов</Typography>
          <Typography variant="body2" color="text.secondary">·</Typography>
          <Typography variant="body2" color="text.secondary">{data.portfolio.artifacts.length} артефактов</Typography>
          {topCompetency && (
            <>
              <Typography variant="body2" color="text.secondary">·</Typography>
              <Typography variant="body2" fontWeight={600}>Сильная сторона: {competencyLabels[topCompetency.competency]}</Typography>
            </>
          )}
        </Stack>
      </Box>

      <Grid container spacing={3} sx={{ mb: 3 }}>
        {['Портфолио развивается', 'Готово к демонстрации', 'Есть рекомендации от заказчика', 'Есть завершённые кейсы'].map((label, index) => (
          <Grid item xs={12} sm={6} md={3} key={label}>
            <StatCard
              title={label}
              value={index === 3 ? data.portfolio.completedCases.length : 'Да'}
              icon={<WorkspacePremiumRoundedIcon />}
              color="primary"
            />
          </Grid>
        ))}
      </Grid>
      <Grid container spacing={3}>
        <Grid item xs={12} lg={5}>
          <CompetencyRadarChart data={radar} />
        </Grid>
        <Grid item xs={12} lg={7}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 3 }}>Матрица компетенций</Typography>
              <CompetencyExplanationCards />
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} lg={7}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 3 }}>Артефакты</Typography>
              <Stack spacing={2}>
                {artifacts.map((item, index) => {
                  const isDoc = item.type === 'Документ';
                  const ready = item.status === 'Готово к демонстрации';
                  return (
                    <Paper
                      key={item.artifact}
                      variant="outlined"
                      sx={{ p: 2.5, transition: 'border-color .15s', '&:hover': { borderColor: 'primary.main' } }}
                    >
                      <Stack direction="row" spacing={2.5} alignItems="center">
                        <Box sx={{
                          width: 40,
                          height: 40,
                          borderRadius: 1.5,
                          display: 'grid',
                          placeItems: 'center',
                          bgcolor: 'background.default',
                          color: 'primary.main'
                        }}>
                          {isDoc ? <DescriptionRoundedIcon /> : <SchemaRoundedIcon />}
                        </Box>
                        <Box sx={{ flexGrow: 1, minWidth: 0 }}>
                          <Typography fontWeight={600} noWrap>{item.artifact}</Typography>
                          <Typography variant="body2" color="text.secondary" noWrap>{item.caseTitle} · {item.type} · {item.date}</Typography>
                        </Box>
                        <Typography variant="body2" color={ready ? 'success.main' : 'text.secondary'} fontWeight={600} sx={{ whiteSpace: 'nowrap' }}>
                          {item.status}
                        </Typography>
                        <Button size="small" endIcon={<ArrowForwardRoundedIcon />}>Открыть</Button>
                      </Stack>
                    </Paper>
                  );
                })}
              </Stack>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} lg={5}>
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2 }}>Ключевые выводы обратной связи</Typography>
              {data.portfolio.feedbackHighlights.map((item) => <Alert key={item} severity="success" sx={{ mb: 1 }}>{item}</Alert>)}
            </CardContent>
          </Card>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2 }}>Рост по кейсам</Typography>
              <Stack spacing={1.5}>
                {data.portfolio.completedCases.map((item, index) => (
                  <Paper key={item} variant="outlined" sx={{ p: 2 }}>
                    <Typography fontWeight={700}>{item}</Typography>
                    <Typography variant="body2" color="text.secondary">Усилены: {competencyLabels[competencyKeys[index % competencyKeys.length]]}, {competencyLabels[competencyKeys[(index + 1) % competencyKeys.length]]}</Typography>
                  </Paper>
                ))}
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}

const reflectionQuestions: { key: string; hint: string }[] = [
  { key: 'Что получилось?', hint: 'Конкретный результат: что заработало, какую задачу закрыли, что стало понятнее.' },
  { key: 'Что было сложно?', hint: 'Где застряли, что заняло больше всего времени, какие места пришлось переделывать.' },
  { key: 'Как использовался ИИ?', hint: 'В чём именно помог наставник — идеи, проверка гипотез, рутина. Где не помог.' },
  { key: 'Что сделал сам?', hint: 'Ваш собственный вклад: решения, которые приняли и обосновали лично.' },
  { key: 'Что улучшить в следующей итерации?', hint: 'Один-два вывода, которые заберёте в следующий кейс.' }
];

export function ReflectionPage() {
  const { submissionId } = useParams();
  const navigate = useNavigate();
  const { session } = useAuth();
  const { data: submissions, loading, error, reload } = useApi(
    () => get<Submission[]>('/submissions', { studentId: session?.user.id }),
    [session?.user.id]
  );

  if (error) return <ErrorState message={error} onRetry={reload} />;
  if (loading || !submissions) return <LoadingBlock />;
  if (!submissionId) {
    return <ReflectionCasePicker submissions={submissions} />;
  }
  const submission = submissions.find((item) => item.id === submissionId);
  return <ReflectionEditor submission={submission} onBack={() => navigate('/student/reflection')} />;
}

function ReflectionCasePicker({ submissions }: { submissions: Submission[] }) {
  const navigate = useNavigate();
  const pending = submissions.filter((item) => !item.reflectionId);
  const done = submissions.filter((item) => item.reflectionId);

  return (
    <Box>
      <PageHeader title="Рефлексия" subtitle="Фиксация вклада, работы с наставником и следующей итерации" />

      <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 2 }}>Ждут рефлексии</Typography>
      {pending.length === 0 ? (
        <EmptyState title="Все рефлексии заполнены" description="Как только появится новое принятое решение, оно попадёт сюда." />
      ) : (
        <Stack spacing={1.5} sx={{ mb: 4 }}>
          {pending.map((submission) => (
            <Paper
              key={submission.id}
              variant="outlined"
              sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 2, cursor: 'pointer', transition: 'border-color 150ms ease', '&:hover': { borderColor: 'primary.main', bgcolor: 'action.hover' } }}
              onClick={() => navigate(`/student/reflection/${submission.id}`)}
            >
              <Box sx={{ width: 38, height: 38, borderRadius: 1.5, bgcolor: 'background.default', color: brand.forest, display: 'grid', placeItems: 'center', flexShrink: 0 }}>
                <SelfImprovementRoundedIcon fontSize="small" />
              </Box>
              <Box sx={{ flexGrow: 1, minWidth: 0 }}>
                <Typography fontWeight={700} noWrap>{submission.title}</Typography>
                <Typography variant="caption" color="text.disabled">{submissionStatusLabels[submission.status]}</Typography>
              </Box>
              <ArrowForwardRoundedIcon fontSize="small" sx={{ color: 'text.disabled', flexShrink: 0 }} />
            </Paper>
          ))}
        </Stack>
      )}

      <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 2 }}>Заполненные</Typography>
      {done.length === 0 ? (
        <Typography color="text.secondary">Заполненных рефлексий пока нет.</Typography>
      ) : (
        <Stack spacing={1.5}>
          {done.map((submission) => (
            <Paper
              key={submission.id}
              variant="outlined"
              sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 2, cursor: 'pointer' }}
              onClick={() => navigate(`/student/reflection/${submission.id}`)}
            >
              <CheckRoundedIcon fontSize="small" sx={{ color: 'success.main', flexShrink: 0 }} />
              <Typography sx={{ flexGrow: 1 }} noWrap>{submission.title}</Typography>
            </Paper>
          ))}
        </Stack>
      )}
    </Box>
  );
}

function ReflectionEditor({ submission, onBack }: { submission?: Submission; onBack: () => void }) {
  const { session } = useAuth();
  const [answers, setAnswers] = useState<Record<string, string>>(
    Object.fromEntries(reflectionQuestions.map((q) => [q.key, '']))
  );
  const [summary, setSummary] = useState('');
  const [saved, setSaved] = useState(false);
  const filledCount = reflectionQuestions.filter((q) => answers[q.key].trim()).length;

  async function submitReflection() {
    if (!submission) return;
    await post('/reflections', { submissionId: submission.id, studentId: session?.user.id, answers, summary });
    setSaved(true);
  }

  const progress = Math.round((filledCount / reflectionQuestions.length) * 100);

  return (
    <Box>
      <Button startIcon={<ArrowForwardRoundedIcon sx={{ transform: 'rotate(180deg)' }} />} onClick={onBack} sx={{ mb: 2 }}>
        К списку кейсов
      </Button>

      <Box sx={{ mb: 4 }}>
        <Typography variant="h3" sx={{ mb: 1 }}>Рефлексия по кейсу</Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
          {submission?.title ?? 'Итоговая фиксация собственного вклада, работы с ИИ и следующей итерации'}
        </Typography>
        <Stack direction="row" spacing={3} alignItems="center">
          <Box>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>Заполнено</Typography>
            <Typography variant="h4">{progress}%</Typography>
          </Box>
          <LinearProgress
            variant="determinate"
            value={progress}
            sx={{ flexGrow: 1, height: 6, borderRadius: 3, bgcolor: 'primary.light', '& .MuiLinearProgress-bar': { bgcolor: 'primary.main', borderRadius: 3 } }}
          />
        </Stack>
      </Box>

      {saved && <Alert severity="success" sx={{ mb: 3 }}>Рефлексия сохранена</Alert>}
      {!submission && <Alert severity="info" sx={{ mb: 3 }}>Кейс не найден — выберите его из списка.</Alert>}

      <Grid container spacing={3}>
        <Grid item xs={12} lg={8}>
          <Stack spacing={2.5}>
            {reflectionQuestions.map((q, index) => {
              const filled = answers[q.key].trim().length > 0;
              return (
                <Card key={q.key} variant="outlined" sx={{ borderColor: filled ? 'primary.main' : 'divider', transition: 'border-color .15s' }}>
                  <CardContent>
                    <Stack direction="row" spacing={2.5} alignItems="flex-start">
                      <Box sx={{
                        width: 32,
                        height: 32,
                        borderRadius: 1.5,
                        display: 'grid',
                        placeItems: 'center',
                        bgcolor: 'background.default',
                        color: 'text.secondary',
                        fontWeight: 700,
                        flexShrink: 0
                      }}>
                        {index + 1}
                      </Box>
                      <Box sx={{ flexGrow: 1 }}>
                        <Typography fontWeight={600} sx={{ mb: 0.5 }}>{q.key}</Typography>
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>{q.hint}</Typography>
                        <TextField
                          value={answers[q.key]}
                          onChange={(event) => setAnswers({ ...answers, [q.key]: event.target.value })}
                          multiline
                          minRows={2}
                          fullWidth
                          placeholder="Ваш ответ…"
                          size="small"
                        />
                      </Box>
                    </Stack>
                  </CardContent>
                </Card>
              );
            })}
          </Stack>
        </Grid>

        <Grid item xs={12} lg={4}>
          <Stack spacing={2.5} sx={{ position: { lg: 'sticky' }, top: { lg: 88 } }}>
            <Card variant="outlined">
              <CardContent>
                <Typography fontWeight={600} sx={{ mb: 0.5 }}>Краткий итог</Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Одно-два предложения — суть того, что вы забираете из кейса
                </Typography>
                <TextField value={summary} onChange={(event) => setSummary(event.target.value)} multiline minRows={4} fullWidth placeholder="Главный вывод…" size="small" />
              </CardContent>
            </Card>

            <Card variant="outlined">
              <CardContent>
                <Stack spacing={2}>
                  <Stack direction="row" justifyContent="space-between" alignItems="center">
                    <Typography variant="body2" color="text.secondary">Заполнено вопросов</Typography>
                    <Typography fontWeight={600}>{filledCount} / {reflectionQuestions.length}</Typography>
                  </Stack>
                  <LinearProgress
                    variant="determinate"
                    value={(filledCount / reflectionQuestions.length) * 100}
                    sx={{ height: 6, borderRadius: 3 }}
                  />
                  <Button variant="contained" size="large" onClick={submitReflection} disabled={!submission}>
                    Сохранить рефлексию
                  </Button>
                </Stack>
              </CardContent>
            </Card>
          </Stack>
        </Grid>
      </Grid>
    </Box>
  );
}

const newsCategoryLabels: Record<NewsCategory, string> = {
  TRACK: 'Трек',
  EVENT: 'Мероприятие',
  PRODUCT: 'Продукт'
};

function formatNewsDate(iso: string) {
  const date = new Date(iso);
  return date.toLocaleDateString('ru-RU', { day: 'numeric', month: 'long' }) + ', ' +
    date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
}

function formatCount(value: number) {
  if (value >= 1000) return `${(value / 1000).toFixed(1).replace('.0', '')} тыс.`;
  return String(value);
}

export function StudentNewsPage() {
  const [category, setCategory] = useState<NewsCategory | 'ALL'>('ALL');
  const { data: posts, loading, error, reload } = useApi(
    () => platformApi.news.list(category === 'ALL' ? undefined : category),
    [category]
  );

  if (error) return <ErrorState message={error} onRetry={reload} />;
  if (loading || !posts) return <LoadingBlock />;

  return (
    <Box>
      <PageHeader title="Новости платформы" subtitle="Обновления треков, анонсы мероприятий и результаты когорт" />

      <Stack direction="row" spacing={1} sx={{ mb: 3 }}>
        {(['ALL', 'TRACK', 'EVENT', 'PRODUCT'] as const).map((value) => (
          <Chip
            key={value}
            label={value === 'ALL' ? 'Все' : newsCategoryLabels[value]}
            onClick={() => setCategory(value)}
            color={category === value ? 'primary' : 'default'}
            variant={category === value ? 'filled' : 'outlined'}
          />
        ))}
      </Stack>

      {posts.length === 0 ? (
        <EmptyState title="Пока нет новостей" description="Загляните позже — здесь появятся обновления платформы." />
      ) : (
        <Stack spacing={2.5}>
          {posts.map((post) => (
            <Card key={post.id}>
              <CardContent>
                <Stack direction="row" spacing={1.5} alignItems="center" sx={{ mb: 2 }}>
                  <Box
                    sx={{
                      width: 34,
                      height: 34,
                      borderRadius: 1.5,
                      display: 'grid',
                      placeItems: 'center',
                      flexShrink: 0,
                      fontWeight: 800,
                      fontFamily: 'Manrope, sans-serif',
                      fontSize: 13,
                      color: '#fff',
                      background: `linear-gradient(135deg, ${brand.forest}, ${brand.teal})`
                    }}
                  >
                    {post.authorInitial}
                  </Box>
                  <Box sx={{ minWidth: 0, flexGrow: 1 }}>
                    <Typography fontWeight={700} noWrap>{post.authorName}</Typography>
                    <Typography variant="caption" color="text.disabled">{formatNewsDate(post.publishedAt)}</Typography>
                  </Box>
                  {post.pinned && <Chip label="Важное" size="small" color="warning" />}
                </Stack>

                <Chip
                  label={newsCategoryLabels[post.category]}
                  size="small"
                  sx={{ mb: 1.5, bgcolor: alpha(brand.teal, 0.12), color: brand.forest, fontWeight: 700, fontSize: 10.5 }}
                />
                <Typography variant="h6" sx={{ mb: 1 }}>{post.title}</Typography>
                <Typography color="text.secondary" sx={{ mb: post.imageLabels.length ? 2 : 2.5 }}>{post.body}</Typography>

                {post.imageLabels.length > 0 && (
                  <Grid container spacing={1} sx={{ mb: 2.5 }}>
                    {post.imageLabels.map((label, index) => (
                      <Grid item xs={12 / Math.min(post.imageLabels.length, 3)} key={label}>
                        <Box
                          sx={{
                            aspectRatio: '4 / 3',
                            borderRadius: 2,
                            display: 'flex',
                            alignItems: 'flex-end',
                            p: 1.5,
                            color: '#fff',
                            fontFamily: 'Manrope, sans-serif',
                            fontWeight: 800,
                            fontSize: 13,
                            lineHeight: 1.25,
                            background: index % 2 === 0
                              ? `linear-gradient(160deg, ${brand.ink}, ${brand.teal})`
                              : `linear-gradient(160deg, #111, ${brand.lime})`
                          }}
                        >
                          {label}
                        </Box>
                      </Grid>
                    ))}
                  </Grid>
                )}

                <Stack direction="row" spacing={3} sx={{ color: 'text.disabled' }}>
                  <Stack direction="row" spacing={0.75} alignItems="center">
                    <FavoriteBorderRoundedIcon sx={{ fontSize: 17 }} />
                    <Typography variant="body2">{post.likes}</Typography>
                  </Stack>
                  <Stack direction="row" spacing={0.75} alignItems="center">
                    <ChatBubbleOutlineRoundedIcon sx={{ fontSize: 17 }} />
                    <Typography variant="body2">{post.comments}</Typography>
                  </Stack>
                  <Stack direction="row" spacing={0.75} alignItems="center">
                    <VisibilityRoundedIcon sx={{ fontSize: 17 }} />
                    <Typography variant="body2">{formatCount(post.views)}</Typography>
                  </Stack>
                </Stack>
              </CardContent>
            </Card>
          ))}
        </Stack>
      )}
    </Box>
  );
}

function EditableTagsCard({
  title,
  tags,
  onChange
}: {
  title: string;
  tags: string[];
  onChange: (next: string[]) => void;
}) {
  const [draft, setDraft] = useState('');

  function addTag() {
    const value = draft.trim();
    if (value && !tags.includes(value)) {
      onChange([...tags, value]);
    }
    setDraft('');
  }

  return (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 1.5 }}>{title}</Typography>
        <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap sx={{ mb: 1.5 }}>
          {tags.map((tag) => (
            <Chip key={tag} label={tag} size="small" onDelete={() => onChange(tags.filter((item) => item !== tag))} />
          ))}
        </Stack>
        <TextField
          size="small"
          fullWidth
          placeholder="Добавить и нажать Enter"
          value={draft}
          onChange={(event) => setDraft(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === 'Enter') {
              event.preventDefault();
              addTag();
            }
          }}
          onBlur={addTag}
        />
      </CardContent>
    </Card>
  );
}

export function StudentProfilePage() {
  const { session } = useAuth();
  const { data, loading, error, reload } = useApi(async () => {
    const [portfolio, submissions, traits] = await Promise.all([
      get<Portfolio>('/portfolio/me'),
      get<Submission[]>('/submissions', { studentId: session?.user.id }),
      platformApi.profileTraits.me()
    ]);
    return { portfolio, submissions, traits };
  }, [session?.user.id]);

  if (error) return <ErrorState message={error} onRetry={reload} />;
  if (loading || !data || !session) return <LoadingBlock />;
  const traits = data.traits;
  const completion = traits.psychotypeCompleted ? 100 : 75;

  async function saveTraits(patch: Partial<Pick<ProfileTraits, 'professionalTags' | 'interests' | 'motivations'>>) {
    await platformApi.profileTraits.updateMe({
      professionalTags: patch.professionalTags ?? traits.professionalTags,
      interests: patch.interests ?? traits.interests,
      motivations: patch.motivations ?? traits.motivations
    });
    await reload();
  }

  const timelineEvents = [
    { date: session.user.createdAt, title: 'Регистрация на платформе', subtitle: session.user.email },
    ...data.submissions
      .filter((item) => item.submittedAt)
      .map((item) => ({
        date: item.submittedAt as string,
        title: item.status === 'ACCEPTED' ? `Решение принято: ${item.title}` : `Решение отправлено: ${item.title}`,
        subtitle: submissionStatusLabels[item.status]
      }))
  ].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());

  return (
    <Box>
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={3} alignItems={{ sm: 'center' }}>
            <Box
              sx={{
                width: 88,
                height: 88,
                borderRadius: 3,
                display: 'grid',
                placeItems: 'center',
                flexShrink: 0,
                color: '#fff',
                fontFamily: 'Manrope, sans-serif',
                fontWeight: 800,
                fontSize: 30,
                background: `linear-gradient(135deg, ${brand.forest} 0%, ${brand.teal} 60%, ${brand.lime} 100%)`
              }}
            >
              {session.user.fullName[0]}
            </Box>
            <Box sx={{ flexGrow: 1, minWidth: 0 }}>
              <Typography variant="h5" sx={{ mb: 0.5 }}>{session.user.fullName}</Typography>
              <Typography color="text.secondary" sx={{ mb: 1.5 }}>Студент · {data.portfolio.summary}</Typography>
              <Stack direction="row" spacing={3} flexWrap="wrap">
                <Typography variant="body2" color="text.secondary">
                  Email: <Typography component="span" fontWeight={600} color="text.primary">{session.user.email}</Typography>
                </Typography>
              </Stack>
            </Box>
            <Stack alignItems="center" spacing={0.5} sx={{ flexShrink: 0 }}>
              <Box sx={{ position: 'relative', width: 62, height: 62 }}>
                <svg width="62" height="62" viewBox="0 0 62 62">
                  <circle cx="31" cy="31" r="26" fill="none" stroke={alpha(brand.stone2, 0.16)} strokeWidth="7" />
                  <circle
                    cx="31" cy="31" r="26" fill="none" stroke={brand.blue} strokeWidth="7" strokeLinecap="round"
                    strokeDasharray={163.4}
                    strokeDashoffset={163.4 * (1 - completion / 100)}
                    transform="rotate(-90 31 31)"
                  />
                  <text x="31" y="36" textAnchor="middle" fontFamily="Manrope, sans-serif" fontWeight={800} fontSize={15}>{completion}%</text>
                </svg>
              </Box>
              <Typography variant="caption" color="text.secondary" textAlign="center">Заполненность<br />профиля</Typography>
            </Stack>
          </Stack>
        </CardContent>
      </Card>

      <Grid container spacing={2} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6}>
          <EditableTagsCard
            title="Профтеги"
            tags={traits.professionalTags}
            onChange={(next) => saveTraits({ professionalTags: next })}
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <EditableTagsCard
            title="Интересы"
            tags={traits.interests}
            onChange={(next) => saveTraits({ interests: next })}
          />
        </Grid>
      </Grid>

      <Typography variant="h6" sx={{ mb: 2 }}>Опыт и достижения</Typography>
      <Grid container spacing={3} sx={{ mb: 5 }}>
        <Grid item xs={12} lg={7}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 2 }}>Завершённые кейсы</Typography>
              {data.portfolio.completedCases.length ? (
                <Stack spacing={1.5}>
                  {data.portfolio.completedCases.map((title) => (
                    <Stack key={title} direction="row" spacing={1.5} alignItems="flex-start">
                      <CheckRoundedIcon fontSize="small" sx={{ color: 'success.main', mt: 0.25 }} />
                      <Typography>{title}</Typography>
                    </Stack>
                  ))}
                </Stack>
              ) : (
                <Typography color="text.secondary">Пока нет завершённых кейсов.</Typography>
              )}
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} lg={5}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 2 }}>Достижения</Typography>
              {data.portfolio.feedbackHighlights.length ? (
                <Stack spacing={1.5}>
                  {data.portfolio.feedbackHighlights.slice(0, 3).map((text) => (
                    <Stack key={text} direction="row" spacing={1.5} alignItems="flex-start">
                      <WorkspacePremiumRoundedIcon fontSize="small" sx={{ color: 'warning.main', mt: 0.25 }} />
                      <Typography variant="body2">{text}</Typography>
                    </Stack>
                  ))}
                </Stack>
              ) : (
                <Typography color="text.secondary">Достижения появятся после первых решений.</Typography>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Typography variant="h6" sx={{ mb: 2 }}>Хронология</Typography>
      <Card sx={{ mb: 5 }}>
        <CardContent>
          {timelineEvents.length ? (
            <Stack spacing={0}>
              {timelineEvents.map((event, index) => (
                <Stack key={`${event.date}-${index}`} direction="row" spacing={2}>
                  <Stack alignItems="center" sx={{ flexShrink: 0 }}>
                    <Box sx={{ width: 10, height: 10, borderRadius: '50%', bgcolor: brand.teal, mt: 0.6 }} />
                    {index < timelineEvents.length - 1 && (
                      <Box sx={{ width: 2, flexGrow: 1, bgcolor: 'divider', my: 0.5 }} />
                    )}
                  </Stack>
                  <Box sx={{ pb: index < timelineEvents.length - 1 ? 2.5 : 0, minWidth: 0 }}>
                    <Typography variant="caption" color="text.disabled" fontWeight={600}>
                      {new Date(event.date).toLocaleDateString('ru-RU', { day: 'numeric', month: 'long', year: 'numeric' })}
                    </Typography>
                    <Typography fontWeight={700}>{event.title}</Typography>
                    <Typography variant="body2" color="text.secondary">{event.subtitle}</Typography>
                  </Box>
                </Stack>
              ))}
            </Stack>
          ) : (
            <Typography color="text.secondary">История появится после первых действий на платформе.</Typography>
          )}
        </CardContent>
      </Card>

      <Typography variant="h6" sx={{ mb: 2 }}>Образование</Typography>
      <Card sx={{ mb: 5 }}>
        <CardContent>
          <Typography color="text.secondary">Раздел образования пока не заполнен. Добавьте сведения об обучении в настройках профиля.</Typography>
        </CardContent>
      </Card>

      <Typography variant="h6" sx={{ mb: 2 }}>Характеристики</Typography>
      <Stack spacing={2.5}>
        <Card>
          <CardContent>
            <Stack direction="row" spacing={2} alignItems="center">
              <Box sx={{ width: 52, height: 52, borderRadius: 2, bgcolor: 'background.default', display: 'grid', placeItems: 'center', flexShrink: 0 }}>
                <SelfImprovementRoundedIcon sx={{ color: brand.forest }} />
              </Box>
              <Box sx={{ flexGrow: 1 }}>
                <Typography fontWeight={700}>Базовый психотип</Typography>
                <Typography variant="body2" color="text.secondary">Пройдите тесты и опросы, чтобы точнее увидеть свои сильные стороны и получить рекомендации по развитию.</Typography>
              </Box>
              <Button variant="outlined" sx={{ flexShrink: 0 }}>Пройти тест</Button>
            </Stack>
          </CardContent>
        </Card>

        <EditableTagsCard
          title="Мотивация"
          tags={traits.motivations}
          onChange={(next) => saveTraits({ motivations: next })}
        />

        <Card>
          <CardContent>
            <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 2 }}>
              <Typography variant="subtitle1" fontWeight={700}>Способности</Typography>
              <Chip label="вычисляется системой" size="small" variant="outlined" />
            </Stack>
            <Stack direction="row" spacing={4} flexWrap="wrap">
              {traits.abilities.map((ability) => (
                <Stack key={ability.label} alignItems="center" spacing={1} sx={{ width: 120 }}>
                  <svg width="52" height="52" viewBox="0 0 52 52">
                    <circle cx="26" cy="26" r="21" fill="none" stroke={alpha(brand.stone2, 0.16)} strokeWidth="6" />
                    <circle
                      cx="26" cy="26" r="21" fill="none" stroke={brand.teal} strokeWidth="6" strokeLinecap="round"
                      strokeDasharray={131.9}
                      strokeDashoffset={131.9 * (1 - ability.percent / 100)}
                      transform="rotate(-90 26 26)"
                    />
                    <text x="26" y="31" textAnchor="middle" fontFamily="Manrope, sans-serif" fontWeight={800} fontSize={13}>{ability.percent}%</text>
                  </svg>
                  <Typography variant="caption" textAlign="center" color="text.secondary">{ability.label}</Typography>
                </Stack>
              ))}
            </Stack>
          </CardContent>
        </Card>

        <Card>
          <CardContent>
            <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 1.5 }}>Навыки</Typography>
            <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap sx={{ mb: 1 }}>
              {traits.selfRatedSkills.map((skill) => (
                <Chip key={skill.label} label={`${skill.label} · ${skill.level}`} size="small" />
              ))}
            </Stack>
            <Typography variant="caption" color="text.disabled">с самооценкой · 1 — начальный, 4 — экспертный</Typography>
          </CardContent>
        </Card>
      </Stack>
    </Box>
  );
}

function CaseCard({ item, onOpen }: { item: PracticalCase; onOpen: () => void }) {
  return (
    <Card sx={{
      height: '100%',
      cursor: 'pointer',
      transition: 'all 150ms ease',
      '&:hover': {
        transform: 'translateY(-2px)',
        borderColor: 'primary.main'
      }
    }}
    onClick={onOpen}
    >
      <CardContent>
        <Typography variant="h6" sx={{ mb: 2 }}>{item.title}</Typography>
        <Typography color="text.secondary" sx={{ mb: 3 }}>{item.shortDescription}</Typography>
        <Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 2 }}>
          <Typography variant="body2" fontWeight={600}>{difficultyLabels[item.difficulty]}</Typography>
          <Typography variant="body2" color="text.secondary">·</Typography>
          <Typography variant="body2" color="text.secondary">{feedbackModeLabels[item.feedbackMode]}</Typography>
        </Stack>
        <Box>
          <Typography variant="caption" color="text.secondary" sx={{ mb: 1, display: 'block' }}>Вклад в компетенции</Typography>
          <LinearProgress variant="determinate" value={Math.max(...Object.values(item.competencyWeights))} sx={{ height: 6, borderRadius: 3 }} />
        </Box>
      </CardContent>
    </Card>
  );
}

function MentorChatPanel({
  title,
  caseTitle,
  artifacts,
  session,
  message,
  onMessage,
  onSend,
  sending = false,
  pendingMessage,
  error,
  restoring = false,
  restoreError,
  onRetryRestore
}: {
  title: string;
  caseTitle: string;
  artifacts: string[];
  session: AgentSession | null;
  message: string;
  onMessage: (value: string) => void;
  onSend: () => void;
  sending?: boolean;
  pendingMessage?: string;
  error?: string | null;
  restoring?: boolean;
  restoreError?: string | null;
  onRetryRestore?: () => void;
}) {
  return (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Typography variant="h6" sx={{ mb: 2 }}>{title}</Typography>
        <Paper
          variant="outlined"
          sx={{ p: 2, mb: 3, bgcolor: 'background.default' }}
        >
          <Typography variant="caption" fontWeight={600} color="text.secondary" sx={{ textTransform: 'uppercase', letterSpacing: '0.05em', mb: 1, display: 'block' }}>
            Контекст наставника
          </Typography>
          <Typography variant="body2" sx={{ mb: 0.5 }}><Typography component="span" fontWeight={600}>Кейс:</Typography> {caseTitle}</Typography>
          <Typography variant="body2"><Typography component="span" fontWeight={600}>Наработки:</Typography> {artifacts.length ? artifacts.join(', ') : 'Не выбраны'}</Typography>
        </Paper>
        {restoring && <Box role="status" sx={{ mb: 2 }}><LinearProgress /><Typography variant="body2">Загружаем историю чата…</Typography></Box>}
        {restoreError && <Alert severity="error" sx={{ mb: 2 }} action={<Button color="inherit" onClick={onRetryRestore}>Повторить загрузку</Button>}>{restoreError}</Alert>}
        {!restoreError && error && <Alert severity="error" sx={{ mb: 2 }} role="alert">{error}</Alert>}
        <Stack spacing={2} sx={{ minHeight: 360, maxHeight: 500, overflow: 'auto', mb: 3 }}>
          {(session?.messages ?? []).map((item) => {
            const isAgent = item.role === 'AGENT';
            return (
              <Stack
                key={item.id}
                direction="row"
                spacing={1.5}
                alignItems="flex-start"
                sx={{ flexDirection: isAgent ? 'row' : 'row-reverse', alignSelf: isAgent ? 'flex-start' : 'flex-end', maxWidth: '88%' }}
              >
                {isAgent ? (
                  <Box sx={{ width: 32, height: 32, borderRadius: 1.5, flexShrink: 0, display: 'grid', placeItems: 'center', bgcolor: 'primary.main', color: 'white' }}>
                    <SmartToyRoundedIcon sx={{ fontSize: 18 }} />
                  </Box>
                ) : (
                  <Box sx={{ width: 32, height: 32, borderRadius: 1.5, flexShrink: 0, display: 'grid', placeItems: 'center', bgcolor: 'secondary.main', color: 'white', fontWeight: 700, fontSize: 14 }}>Вы</Box>
                )}
                <Box
                  sx={{
                    p: 2,
                    borderRadius: 2,
                    bgcolor: isAgent ? 'background.default' : 'primary.main',
                    color: isAgent ? 'text.primary' : 'white',
                    border: '1px solid',
                    borderColor: isAgent ? 'divider' : 'transparent'
                  }}
                >
                  <Typography variant="caption" fontWeight={600} sx={{ display: 'block', mb: 0.5, color: isAgent ? 'text.secondary' : alpha('#fff', 0.85) }}>
                    {isAgent ? 'Наставник' : 'Вы'}
                  </Typography>
                  <Typography variant="body2" sx={{ whiteSpace: 'pre-line' }}>{item.content}</Typography>
                </Box>
              </Stack>
            );
          })}
          {sending && pendingMessage && (
            <Stack direction="row" spacing={1.5} alignItems="flex-start" sx={{ flexDirection: 'row-reverse', alignSelf: 'flex-end', maxWidth: '88%' }}>
              <Box sx={{ width: 32, height: 32, borderRadius: 1.5, flexShrink: 0, display: 'grid', placeItems: 'center', bgcolor: 'secondary.main', color: 'white', fontWeight: 700, fontSize: 14 }}>Вы</Box>
              <Box sx={{ p: 2, borderRadius: 2, bgcolor: 'primary.main', color: 'white', opacity: 0.7 }}>
                <Typography variant="caption" fontWeight={600} sx={{ display: 'block', mb: 0.5, color: alpha('#fff', 0.85) }}>Вы</Typography>
                <Typography variant="body2" sx={{ whiteSpace: 'pre-line' }}>{pendingMessage}</Typography>
              </Box>
            </Stack>
          )}
          {sending && (
            <Stack direction="row" spacing={1.5} alignItems="flex-start" sx={{ alignSelf: 'flex-start', maxWidth: '88%' }}>
              <Box sx={{ width: 32, height: 32, borderRadius: 1.5, flexShrink: 0, display: 'grid', placeItems: 'center', bgcolor: 'primary.main', color: 'white' }}>
                <SmartToyRoundedIcon sx={{ fontSize: 18 }} />
              </Box>
              <Box sx={{ p: 2, borderRadius: 2, bgcolor: 'background.default', border: '1px solid', borderColor: 'divider', display: 'flex', alignItems: 'center', gap: 1 }}>
                <CircularProgress size={14} />
                <Typography variant="body2" color="text.secondary">Наставник печатает…</Typography>
              </Box>
            </Stack>
          )}
          {!session && !sending && !restoring && !restoreError && <EmptyState title="Чат готов" description="Задайте вопрос по цели, структуре решения или проверке гипотез." />}
        </Stack>
        <Stack direction="row" spacing={1.5}>
          <TextField
            value={message}
            onChange={(event) => onMessage(event.target.value)}
            onKeyDown={(event) => { if (event.key === 'Enter' && !event.shiftKey) { event.preventDefault(); onSend(); } }}
            placeholder="Помоги проверить структуру решения"
            fullWidth
            size="small"
            disabled={sending}
          />
          <Button aria-label="Отправить сообщение" variant="contained" onClick={onSend} disabled={sending || restoring || Boolean(restoreError) || !message.trim()} sx={{ px: 3 }}>
            {sending ? <CircularProgress size={20} color="inherit" /> : <SendRoundedIcon />}
          </Button>
        </Stack>
      </CardContent>
    </Card>
  );
}

function RoadmapTimelineRow({
  step,
  index,
  isFinal,
  title,
  linkedCase,
  onOpenCase,
  onStart,
  onComplete
}: {
  step: { status: RoadmapStatus; title: string; description: string; caseId?: string | null };
  index: number;
  isFinal: boolean;
  title: string;
  linkedCase?: PracticalCase;
  onOpenCase: () => void;
  onStart: () => void;
  onComplete: () => void;
}) {
  const color = nodeColor(step.status);
  const done = step.status === 'COMPLETED';
  const locked = step.status === 'LOCKED';
  const active = step.status === 'IN_PROGRESS' || step.status === 'AVAILABLE';

  const NodeIcon = isFinal
    ? WorkspacePremiumRoundedIcon
    : done
      ? CheckRoundedIcon
      : locked
        ? LockRoundedIcon
        : step.status === 'IN_PROGRESS'
          ? PlayArrowRoundedIcon
          : TimelineRoundedIcon;

  return (
    <Stack direction="row" spacing={3} sx={{ opacity: locked ? 0.6 : 1 }}>
      <Stack alignItems="center" sx={{ pt: 0.5 }}>
        <Box
          sx={{
            width: 40,
            height: 40,
            borderRadius: 1.5,
            display: 'grid',
            placeItems: 'center',
            flexShrink: 0,
            color: 'white',
            bgcolor: color,
            border: active ? `3px solid ${alpha(color, 0.2)}` : 'none'
          }}
        >
          <NodeIcon fontSize="small" />
        </Box>
        {!isFinal && (
          <Box
            sx={{
              flexGrow: 1,
              width: 2,
              minHeight: 32,
              my: 1.5,
              borderRadius: 1,
              bgcolor: done ? nodeColor('COMPLETED') : 'divider'
            }}
          />
        )}
      </Stack>

      <Card
        variant="outlined"
        sx={{
          flexGrow: 1,
          mb: 3,
          borderColor: active ? 'primary.main' : 'divider',
          transition: 'border-color .2s'
        }}
      >
        <CardContent>
          <Stack direction="row" justifyContent="space-between" alignItems="flex-start" spacing={2} sx={{ mb: 2 }}>
            <Box>
              <Typography variant="caption" color="text.secondary" fontWeight={600} sx={{ textTransform: 'uppercase', letterSpacing: '0.05em', mb: 0.5, display: 'block' }}>
                Этап {String(index + 1).padStart(2, '0')}
              </Typography>
              <Typography variant="h6">{title}</Typography>
            </Box>
            <Typography variant="body2" fontWeight={600} color={color}>
              {roadmapStatusLabels[step.status]}
            </Typography>
          </Stack>

          <Typography variant="body2" color="text.secondary" sx={{ mb: linkedCase ? 2 : 0 }}>{step.description}</Typography>

          {linkedCase && (
            <Box sx={{ p: 2, borderRadius: 2, bgcolor: 'background.default' }}>
              <Typography variant="caption" color="text.secondary" fontWeight={600} sx={{ textTransform: 'uppercase', letterSpacing: '0.05em', mb: 1, display: 'block' }}>
                Связанный кейс
              </Typography>
              <Typography variant="body1" fontWeight={600} sx={{ mb: 1.5 }}>{linkedCase.title}</Typography>
              <CompetencyBars values={linkedCase.competencyWeights} compact />
            </Box>
          )}

          {(active) && (
            <Stack direction="row" spacing={1.5} sx={{ mt: 2 }}>
              {step.status === 'AVAILABLE' && linkedCase && (
                <Button size="small" variant="contained" endIcon={<ArrowForwardRoundedIcon />} onClick={onOpenCase}>Перейти к кейсу</Button>
              )}
              {step.status === 'AVAILABLE' && !linkedCase && (
                <Button size="small" variant="contained" onClick={onStart}>Начать этап</Button>
              )}
              {step.status === 'IN_PROGRESS' && (
                <Button size="small" variant="outlined" onClick={onComplete}>Завершить этап</Button>
              )}
            </Stack>
          )}
        </CardContent>
      </Card>
    </Stack>
  );
}

function topCompetencies(values: Partial<Record<Competency, number>>) {
  return competencyKeys
    .slice()
    .sort((left, right) => (values[right] ?? 0) - (values[left] ?? 0))
    .slice(0, 2);
}

function nodeColor(status: RoadmapStatus) {
  const colors: Record<RoadmapStatus, string> = {
    LOCKED: '#94a3b8',
    AVAILABLE: '#2563eb',
    IN_PROGRESS: '#0b7a64',
    COMPLETED: '#16803c'
  };
  return colors[status];
}
