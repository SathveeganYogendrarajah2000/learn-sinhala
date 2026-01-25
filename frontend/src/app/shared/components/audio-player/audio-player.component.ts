import { Component, Input, signal, ElementRef, ViewChild } from '@angular/core';

/**
 * Simple audio player component for pronunciation playback.
 */
@Component({
  selector: 'app-audio-player',
  standalone: true,
  template: `
    <button
      class="audio-btn"
      [class.playing]="playing()"
      [disabled]="!src"
      (click)="togglePlay()"
      [title]="playing() ? 'Stop' : 'Play pronunciation'"
    >
      @if (playing()) {
        <span class="icon">&#9632;</span>
      } @else {
        <span class="icon">&#9658;</span>
      }
    </button>
    <audio #audioEl [src]="src" (ended)="onEnded()"></audio>
  `,
  styles: [`
    :host {
      display: inline-block;
    }

    .audio-btn {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      border: 2px solid var(--primary);
      background: transparent;
      color: var(--primary);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s ease;
    }

    .audio-btn:hover:not(:disabled) {
      background: var(--primary);
      color: white;
    }

    .audio-btn:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .audio-btn.playing {
      background: var(--primary);
      color: white;
    }

    .icon {
      font-size: 1.25rem;
    }

    audio {
      display: none;
    }
  `]
})
export class AudioPlayerComponent {
  @Input() src = '';

  @ViewChild('audioEl') audioRef!: ElementRef<HTMLAudioElement>;

  playing = signal(false);

  togglePlay(): void {
    const audio = this.audioRef?.nativeElement;
    if (!audio) return;

    if (this.playing()) {
      audio.pause();
      audio.currentTime = 0;
      this.playing.set(false);
    } else {
      audio.play();
      this.playing.set(true);
    }
  }

  onEnded(): void {
    this.playing.set(false);
  }
}
