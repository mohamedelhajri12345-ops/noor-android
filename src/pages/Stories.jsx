import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronLeft } from 'lucide-react';
import { stories } from '@/data/stories';
import { toArabicNumber } from '@/lib/islamicUtils';

export default function Stories() {
  const navigate = useNavigate();

  return (
    <div className="space-y-4 animate-fade-in">
      <div className="text-center pt-2">
        <h2 className="text-xl font-bold text-gold">قصص الأنبياء</h2>
        <p className="text-xs text-muted-foreground mt-1">{toArabicNumber(stories.length)} قصة ملهمة</p>
      </div>

      <div className="space-y-3">
        {stories.map(story => (
          <button key={story.id} onClick={() => navigate(`/stories/${story.id}`)}
            className="w-full glass-card pressable rounded-2xl p-4 flex items-center gap-3 hover:glass-hover transition-all text-right">
            <div className="w-12 h-12 rounded-xl bg-gold/10 flex items-center justify-center shrink-0">
              <span className="text-sm font-bold text-gold">{toArabicNumber(story.id)}</span>
            </div>
            <div className="flex-1">
              <p className="text-sm font-bold text-foreground arabic-text">{story.prophet}</p>
              <p className="text-xs text-muted-foreground">{story.title}</p>
            </div>
            <ChevronLeft className="w-4 h-4 text-muted-foreground" />
          </button>
        ))}
      </div>
    </div>
  );
}
