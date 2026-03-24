import { CheckOutlined, CloseOutlined, ReloadOutlined, RightOutlined } from '@ant-design/icons';
import CryptoJS from 'crypto-js';
import { request } from '@umijs/max';
import type { CSSProperties, PointerEvent as ReactPointerEvent } from 'react';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';

const DEFAULT_WIDTH = 310;
const DEFAULT_HEIGHT = 155;
const DEFAULT_BAR_HEIGHT = 40;
const DEFAULT_V_SPACE = 5;
const DEFAULT_EXPLAIN = '向右滑动完成验证';
const DEFAULT_CAPTCHA_TYPE = 'blockPuzzle';
const DEFAULT_TIP_ERROR = '验证码加载失败';
const DEFAULT_VERIFY_ERROR = '验证失败';
const SUCCESS_DELAY_MS = 1000;

interface DragCaptchaFetchResponse {
  repCode: string;
  repMsg?: string;
  repData?: {
    originalImageBase64?: string;
    jigsawImageBase64?: string;
    token?: string;
    secretKey?: string;
  };
}

interface DragCaptchaCheckResponse {
  repCode: string;
  repMsg?: string;
}

export interface DragCaptchaSuccessPayload {
  captchaVerification: string;
  token: string;
}

interface DragCaptchaProps {
  width?: number;
  height?: number;
  barHeight?: number;
  vSpace?: number;
  explain?: string;
  captchaType?: string;
  active?: boolean;
  onSuccess: (payload: DragCaptchaSuccessPayload) => void;
  onRefresh?: () => void;
}

type IconState = 'default' | 'success' | 'error';

function aesEncrypt(word: string, keyWord: string) {
  const key = CryptoJS.enc.Utf8.parse(keyWord);
  const src = CryptoJS.enc.Utf8.parse(word);
  return CryptoJS.AES.encrypt(src, key, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7,
  }).toString();
}

function toImageUrl(base64?: string) {
  if (!base64) {
    return '';
  }
  return base64.startsWith('data:') ? base64 : `data:image/png;base64,${base64}`;
}

export function DragCaptcha({
  width = DEFAULT_WIDTH,
  height = DEFAULT_HEIGHT,
  barHeight = DEFAULT_BAR_HEIGHT,
  vSpace = DEFAULT_V_SPACE,
  explain = DEFAULT_EXPLAIN,
  captchaType = DEFAULT_CAPTCHA_TYPE,
  active = true,
  onSuccess,
  onRefresh,
}: DragCaptchaProps) {
  const [secretKey, setSecretKey] = useState('');
  const [backImgBase, setBackImgBase] = useState('');
  const [blockBackImgBase, setBlockBackImgBase] = useState('');
  const [backToken, setBackToken] = useState('');
  const [tipWords, setTipWords] = useState('');
  const [text, setText] = useState(explain);
  const [moveBlockLeft, setMoveBlockLeft] = useState(0);
  const [leftBarWidth, setLeftBarWidth] = useState<number | null>(null);
  const [moveBlockBackgroundColor, setMoveBlockBackgroundColor] = useState('#fff');
  const [leftBarBorderColor, setLeftBarBorderColor] = useState('#ddd');
  const [iconColor, setIconColor] = useState('#000');
  const [iconState, setIconState] = useState<IconState>('default');
  const [showRefresh, setShowRefresh] = useState(true);
  const [passFlag, setPassFlag] = useState(false);
  const [isEnd, setIsEnd] = useState(false);
  const [transitionLeft, setTransitionLeft] = useState('');
  const [transitionWidth, setTransitionWidth] = useState('');
  const [backImageWidth, setBackImageWidth] = useState(DEFAULT_WIDTH);
  const [blockImageWidth, setBlockImageWidth] = useState(47);
  const barAreaRef = useRef<HTMLDivElement | null>(null);
  const removeListenersRef = useRef<(() => void) | null>(null);
  const resetTimerRef = useRef<number | null>(null);
  const finishTimerRef = useRef<number | null>(null);
  const startLeftRef = useRef(0);
  const startMoveTimeRef = useRef(0);
  const draggingRef = useRef(false);
  const moveBlockLeftRef = useRef(0);
  const isEndRef = useRef(false);

  const blockWidth = useMemo(() => Math.floor((width * 47) / 310), [width]);

  const clearTimers = useCallback(() => {
    if (resetTimerRef.current) {
      window.clearTimeout(resetTimerRef.current);
      resetTimerRef.current = null;
    }
    if (finishTimerRef.current) {
      window.clearTimeout(finishTimerRef.current);
      finishTimerRef.current = null;
    }
  }, []);

  const removeWindowListeners = useCallback(() => {
    removeListenersRef.current?.();
    removeListenersRef.current = null;
  }, []);

  const resetState = useCallback(() => {
    clearTimers();
    removeWindowListeners();
    draggingRef.current = false;
    isEndRef.current = false;
    moveBlockLeftRef.current = 0;
    setText(explain);
    setTransitionLeft('left .3s');
    setTransitionWidth('width .3s');
    setMoveBlockLeft(0);
    setLeftBarWidth(null);
    setLeftBarBorderColor('#ddd');
    setMoveBlockBackgroundColor('#fff');
    setIconColor('#000');
    setIconState('default');
    setIsEnd(false);
    setPassFlag(false);
    setShowRefresh(true);
    resetTimerRef.current = window.setTimeout(() => {
      setTransitionWidth('');
      setTransitionLeft('');
      setText(explain);
      resetTimerRef.current = null;
    }, 300);
  }, [clearTimers, explain, removeWindowListeners]);

  const updateImageSize = useCallback((base64: string, onLoad: (width: number) => void) => {
    const imageUrl = toImageUrl(base64);
    if (!imageUrl) {
      return;
    }
    const image = new Image();
    image.onload = () => {
      onLoad(image.naturalWidth || 0);
    };
    image.src = imageUrl;
  }, []);

  const getPicture = useCallback(async () => {
    try {
      const result = (await request('/auth/captcha/get', {
        method: 'POST',
        data: { captchaType },
        skipErrorHandler: true,
      } as any)) as DragCaptchaFetchResponse;
      if (result.repCode === '0000' && result.repData) {
        const nextBackImgBase = result.repData.originalImageBase64 || '';
        const nextBlockBackImgBase = result.repData.jigsawImageBase64 || '';
        setBackImgBase(nextBackImgBase);
        setBlockBackImgBase(nextBlockBackImgBase);
        setBackToken(result.repData.token || '');
        setSecretKey(result.repData.secretKey || '');
        setTipWords('');
        updateImageSize(nextBackImgBase, (nextWidth) => {
          setBackImageWidth(nextWidth || DEFAULT_WIDTH);
        });
        updateImageSize(nextBlockBackImgBase, (nextWidth) => {
          setBlockImageWidth(nextWidth || 47);
        });
        return;
      }
      setTipWords(result.repMsg || DEFAULT_TIP_ERROR);
    } catch {
      setTipWords(DEFAULT_TIP_ERROR);
    }
  }, [captchaType, updateImageSize]);

  const refresh = useCallback(async () => {
    resetState();
    await getPicture();
    onRefresh?.();
  }, [getPicture, onRefresh, resetState]);

  useEffect(() => {
    if (active) {
      void refresh();
      return;
    }
    clearTimers();
    removeWindowListeners();
  }, [active, clearTimers, refresh, removeWindowListeners]);

  useEffect(() => {
    return () => {
      clearTimers();
      removeWindowListeners();
    };
  }, [clearTimers, removeWindowListeners]);

  const handleMove = useCallback(
    (clientX: number) => {
      if (!draggingRef.current || isEndRef.current || !barAreaRef.current) {
        return;
      }
      const barRect = barAreaRef.current.getBoundingClientRect();
      const barWidth = barAreaRef.current.clientWidth || width;
      const maxLeft = Math.max(barWidth - barHeight, 0);
      let nextMoveLeft = clientX - barRect.left - startLeftRef.current;
      if (nextMoveLeft >= maxLeft) {
        nextMoveLeft = maxLeft;
      }
      if (nextMoveLeft <= 0) {
        nextMoveLeft = 0;
      }
      moveBlockLeftRef.current = nextMoveLeft;
      setMoveBlockLeft(nextMoveLeft);
      setLeftBarWidth(nextMoveLeft + Math.floor(barHeight / 2));
    },
    [barHeight, width],
  );

  const handleVerify = useCallback(async () => {
    if (!draggingRef.current || isEndRef.current) {
      return;
    }
    draggingRef.current = false;
    removeWindowListeners();
    const endMoveTime = Date.now();
    const barWidth = barAreaRef.current?.clientWidth || width;
    const baseWidth = backImageWidth || DEFAULT_WIDTH;
    const baseBlockWidth = blockImageWidth || Math.round((blockWidth / Math.max(width, 1)) * baseWidth);
    const maxCaptchaX = Math.max(baseWidth - baseBlockWidth, 0);
    const mappedX = Math.round((moveBlockLeftRef.current / Math.max(barWidth, 1)) * baseWidth);
    const moveLeftDistance = Math.min(Math.max(mappedX, 0), maxCaptchaX);
    const point = { x: moveLeftDistance, y: 5.0 };
    const pointJson = secretKey ? aesEncrypt(JSON.stringify(point), secretKey) : JSON.stringify(point);
    let result: DragCaptchaCheckResponse;
    try {
      result = (await request('/auth/captcha/check', {
        method: 'POST',
        data: {
          captchaType,
          pointJson,
          token: backToken,
        },
        skipErrorHandler: true,
      } as any)) as DragCaptchaCheckResponse;
    } catch {
      result = { repCode: '0001', repMsg: DEFAULT_VERIFY_ERROR };
    }
    if (result.repCode === '0000') {
      isEndRef.current = true;
      setMoveBlockBackgroundColor('#5cb85c');
      setLeftBarBorderColor('#5cb85c');
      setIconColor('#fff');
      setIconState('success');
      setShowRefresh(false);
      setIsEnd(true);
      setPassFlag(true);
      setTipWords(`${((endMoveTime - startMoveTimeRef.current) / 1000).toFixed(2)}s验证成功`);
      const captchaVerification = secretKey
        ? aesEncrypt(`${backToken}---${JSON.stringify(point)}`, secretKey)
        : `${backToken}---${JSON.stringify(point)}`;
      finishTimerRef.current = window.setTimeout(() => {
        setTipWords('');
        finishTimerRef.current = null;
        onSuccess({ captchaVerification, token: backToken });
      }, SUCCESS_DELAY_MS);
      return;
    }
    setMoveBlockBackgroundColor('#d9534f');
    setLeftBarBorderColor('#d9534f');
    setIconColor('#fff');
    setIconState('error');
    setPassFlag(false);
    setTipWords(result.repMsg || DEFAULT_VERIFY_ERROR);
    finishTimerRef.current = window.setTimeout(() => {
      setTipWords('');
      finishTimerRef.current = null;
      void refresh();
    }, SUCCESS_DELAY_MS);
  }, [
    backImageWidth,
    backToken,
    blockImageWidth,
    blockWidth,
    captchaType,
    onSuccess,
    refresh,
    removeWindowListeners,
    secretKey,
    width,
  ]);

  const attachWindowListeners = useCallback(() => {
    const handleWindowPointerMove = (event: PointerEvent) => {
      event.preventDefault();
      handleMove(event.clientX);
    };
    const handleWindowPointerUp = () => {
      void handleVerify();
    };
    window.addEventListener('pointermove', handleWindowPointerMove, { passive: false });
    window.addEventListener('pointerup', handleWindowPointerUp);
    removeListenersRef.current = () => {
      window.removeEventListener('pointermove', handleWindowPointerMove);
      window.removeEventListener('pointerup', handleWindowPointerUp);
    };
  }, [handleMove, handleVerify]);

  const handleStart = useCallback(
    (event: ReactPointerEvent<HTMLDivElement>) => {
      if (!active || isEndRef.current || !barAreaRef.current) {
        return;
      }
      event.preventDefault();
      const barRect = barAreaRef.current.getBoundingClientRect();
      startLeftRef.current = Math.floor(event.clientX - barRect.left - moveBlockLeftRef.current);
      startMoveTimeRef.current = Date.now();
      setText('');
      setMoveBlockBackgroundColor('#337ab7');
      setLeftBarBorderColor('#337AB7');
      setIconColor('#fff');
      draggingRef.current = true;
      attachWindowListeners();
    },
    [active, attachWindowListeners],
  );

  const iconNode = useMemo(() => {
    if (iconState === 'success') {
      return <CheckOutlined style={{ color: iconColor, fontSize: 18 }} />;
    }
    if (iconState === 'error') {
      return <CloseOutlined style={{ color: iconColor, fontSize: 18 }} />;
    }
    return <RightOutlined style={{ color: iconColor, fontSize: 18 }} />;
  }, [iconColor, iconState]);

  const imagePanelStyle: CSSProperties = {
    position: 'relative',
    width,
    height,
    borderRadius: 6,
    overflow: 'hidden',
    border: '1px solid rgba(5, 5, 5, 0.12)',
    background: '#fff',
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 8, userSelect: 'none' }}>
      <div style={{ position: 'relative', height: height + vSpace }}>
        <div style={imagePanelStyle}>
          {backImgBase ? (
            <img
              src={toImageUrl(backImgBase)}
              alt=""
              draggable={false}
              style={{ width: '100%', height: '100%', display: 'block', objectFit: 'fill' }}
            />
          ) : null}
          {showRefresh ? (
            <button
              type="button"
              onClick={() => void refresh()}
              style={{
                position: 'absolute',
                top: 6,
                right: 6,
                zIndex: 2,
                width: 28,
                height: 28,
                border: 0,
                borderRadius: 14,
                background: 'rgba(255,255,255,0.86)',
                cursor: 'pointer',
              }}
            >
              <ReloadOutlined />
            </button>
          ) : null}
          {tipWords ? (
            <span
              style={{
                position: 'absolute',
                left: 0,
                bottom: 0,
                width: '100%',
                height: 30,
                lineHeight: '30px',
                color: '#fff',
                textAlign: 'center',
                background: passFlag ? 'rgba(92, 184, 92, 0.5)' : 'rgba(217, 83, 79, 0.5)',
              }}
            >
              {tipWords}
            </span>
          ) : null}
        </div>
      </div>
      <div
        ref={barAreaRef}
        style={{
          position: 'relative',
          width,
          height: barHeight,
          lineHeight: `${barHeight}px`,
          background: '#fff',
          textAlign: 'center',
          border: '1px solid #ddd',
          borderRadius: 4,
          overflow: 'hidden',
        }}
      >
        <span style={{ color: '#666' }}>{text}</span>
        <div
          style={{
            position: 'absolute',
            top: -1,
            left: -1,
            width: leftBarWidth !== null ? leftBarWidth : barHeight,
            height: barHeight,
            border: `1px solid ${leftBarBorderColor}`,
            background: '#f0fff0',
            boxSizing: 'content-box',
            transition: transitionWidth,
          }}
        />
        <div
          onPointerDown={handleStart}
          style={{
            position: 'absolute',
            top: 0,
            left: moveBlockLeft,
            width: barHeight,
            height: barHeight,
            background: moveBlockBackgroundColor,
            cursor: isEnd ? 'default' : 'pointer',
            boxShadow: '0 0 2px #888888',
            borderRadius: 1,
            transition: transitionLeft,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            touchAction: 'none',
          }}
        >
          {iconNode}
          <div
            style={{
              position: 'absolute',
              top: -(height + vSpace + 8),
              width: blockWidth,
              height,
              zIndex: 3,
            }}
          >
            {blockBackImgBase ? (
              <img
                src={toImageUrl(blockBackImgBase)}
                alt=""
                draggable={false}
                style={{ width: '100%', height: '100%', display: 'block' }}
              />
            ) : null}
          </div>
        </div>
      </div>
    </div>
  );
}
