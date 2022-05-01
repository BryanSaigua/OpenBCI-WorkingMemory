% clear all; clc; close all;
% 
% fc = 250;
% fs = 250; %785rad/s
% order = 6;
% 
% dataIn = readmatrix('ojosabiertos.txt');
% dataIn1 = readmatrix('ojoscerrados.txt');
% signal  = dataIn(: , [3]);
% signal1  = dataIn1(: , [3]);
% % 
% % 
% SCALE_FACTOR_EEG = (4500000)/24/(2^23-1); %uV/count
% % %signal1 = rand (1, 1000);%
% %signal1 = signal*SCALE_FACTOR_EEG;
% % 
% [b,a] = butter(order,[1 50]/(fs/2),'bandpass');
% 
% % % 
% % Aplicacion del filtro
% filteredSignal = filter(b,a,signal);
% filteredSignal1 = filter(b,a,signal1);
% 
% % 
% % % GrÃ¡ficos
% figure(1);
% plot(signal);
% title('señal original ojos abiertos');
% 
% % original FC
% % % 
% [Spdx,f] = psd_signal(signal,fs,1);
% title('señal original ojos abiertos FC');
% % % 
% figure(3);
% plot(filteredSignal);
% title('señal filtrada ojos abiertos');
% 
% figure;
% plot(signal1);
% title('señal original ojos cerrados');
% 
% 
% [Spdx,f] = psd_signal(signal1,fs,1);
% title('señal original ojos cerrados FC');
% 
% figure;
% plot(filteredSignal1);
% title('señal filtrada ojos cerrados');


% 
% % % SIGNAL FILER
% % 
% [Spdx1,f] = psd_signal(filteredSignal,fs,1);
%  title('señal filtrada FC');
% % % 
% % % 
wo = 60/(fc/2);  
bw = wo/35;
[b1,a1] = iirnotch(wo,bw);
% % % 
% % % 
% % figure(5);
% nochsignal = filter(b1,a1,signal1);
% nochsignal1 = filter(b1,a1,filteredSignal);
% % plot(nochsignal);
% % title('señal noch');
% % SIGNAL NOCH
% 
% [Spdx2,f] = psd_signal(nochsignal1,fs,1);
% title('señal noch FC');
% [Spdx4,f] = psd_signal(nochsignal1,fs,1);
% title('señal tt FC');
% % 
% figure;
% filtertot = Spdx1 + Spdx2;
% filtertot1 = nochsignal + filteredSignal;
% % f=-fs/2:fs/(length(filtertot)-1):fs/2;
% plot(f,filtertot);
% title('señal tot');
% % 
% [Spdx5,f] = psd_signal(filtertot1,fs,1);
% title('señal suma FC');
% 
% figure;
% convolution = conv(filteredSignal,nochsignal);
% plot(convolution);
% title('señal conv');

% [A,B,C,D] = butter(6,[2 54]/fs,'bandpass');
% [b,a] = butter(6,[2 54]/fs,'bandpass');
% h = fvtool(b,a); 
% 
% wo = 60/(fc/2);  
% bw = wo/35;
% [b1,a1] = iirnotch(wo,bw);
% h1 = fvtool(b1,a1)
% figure(3)
% pspectrum(h1,fs)

% d = designfilt('bandpassiir','FilterOrder',6, ...
% 'HalfPowerFrequency1',500,'HalfPowerFrequency2',560, ...
%     'SampleRate',1500);
% sos = ss2sos(A,B,C,D);
% figure(2);
% fvt = fvtool(sos,d,'Fs',250);
% legend(fvt,'butter','designfilt')
% 
% dataIn = readmatrix('text1.txt');
% x = dataIn(:,2);
% % [b,a] = butter(6,fc/(fs/2));
% bandpass(x,[2 54],fs)
% y = bandpass(x,[2 54],fs);
% figure(2);
% bandpass(x,[66 90],fs)
% yy = bandpass(x,[66 90],fs);
% % figure
% % pspectrum(x,[2 54],fs)
% 
% % b = fir1(6,fs,'bandpass')
% 
% [y1,d1] = bandpass(x,[2 54],fs,'ImpulseResponse','iir','Steepness',0.5);
% [y2,d2] = bandpass(y1,[56 90],fs,'ImpulseResponse','iir','Steepness',0.8);
% figure(3);
% pspectrum([y + yy],fs);
% % figure
% % pspectrum(x,fs);
% % freqz(b,a,[],fs)
% % 
% % subplot(2,1,1)
% % ylim([-100 20])

% Fs=250;
% flow=1;
% fhigh=30;
% Or=6;
% 
% data = readmatrix('OpenBCI-RAW-2022-04-13_10-41-07.txt');
% 
% [b,a]=butter(Or,[flow,fhigh]/(Fs/2),'bandpass');
% 
% 
% Fs=250;
% flownot=55;
% fhighnot=65;
% Ornot=1;
% [bnot,anot]=butter(Ornot,[flownot,fhighnot]/(Fs/2),'stop');
% 
% for i=1:16
%  %% SIGNALS
%     for j=1:57
%          data1=data((1+(j-1)*128):(512+(j-1)*128),:);
%          Signal=data1(:,i);
%          Signal=filtfilt(b,a,Signal);
%          Signal=filtfilt(bnot,anot,Signal);
%          fftSignal= abs(fft(Signal));
%          for r=1:57
%              data2=data((1+(r-1)*128):(512+(r-1)*128),:);
%              Sig=data2(:,i);
%              Sig=filtfilt(b,a,Sig);
%              Sig=filtfilt(bnot,anot,Sig);
%              fftSig= abs(fft(Sig));
%              q(r)=max(max(fftSig));
%          end
%     end
% end
%              q=max(q);
%              N = length(Signal);
%              fax_Hz_1 = linspace(0,Fs,N);
%              figure();
%              plot(fax_Hz_1(1:N),fftSignal(1:N));
%              axis([0 64 0 q+1]);
%              xlabel('Frequency (Hz)')
%              ylabel('Amplitud (mV)');
% 

