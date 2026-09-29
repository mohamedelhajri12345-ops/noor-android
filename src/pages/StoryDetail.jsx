import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ChevronRight, BookOpen } from 'lucide-react';
import { stories } from '@/data/stories';

export default function StoryDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const story = stories.find(s => s.id === parseInt(id));

  if (!story) return <div className="text-center text-muted-foreground py-12">القصة غير موجودة</div>;

  return (
    <div className="space-y-4 animate-fade-in">
      <div className="glass-card rounded-2xl p-6">
        <div className="text-center mb-4">
          <div className="w-16 h-16 rounded-2xl bg-gold/10 flex items-center justify-center mx-auto mb-3">
            <BookOpen className="w-7 h-7 text-gold" />
          </div>
          <h2 className="text-xl font-bold text-gold arabic-text">{story.prophet}</h2>
          <p className="text-sm text-muted-foreground mt-1">{story.title}</p>
        </div>

        <div className="h-px bg-gold/20 my-4" />

        <p className="arabic-text text-base text-foreground leading-loose text-justify">{story.text}</p>
      </div>

      {/* Navigation to next/prev story */}
      <div className="flex justify-between gap-2">
        {story.id > 1 ? (
          <button onClick={() => navigate(`/stories/${story.id - 1}`)} className="flex-1 glass-card rounded-xl p-3 text-sm text-foreground hover:glass-hover">
            ← القصة السابقة
          </button>
        ) : <div className="flex-1" />}
        {story.id < stories.length ? (
          <button onClick={() => navigate(`/stories/${story.id + 1}`)} className="flex-1 glass-card rounded-xl p-3 text-sm text-foreground hover:glass-hover">
            القصة التالية →
          </button>
        ) : <div className="flex-1" />}
      </div>
    </div>
  );
}
